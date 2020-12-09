package com.soraxus.prisons.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.base.elements.BunkerElementFlag;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.shops.BunkerSelectiveShop;
import com.soraxus.prisons.bunkers.gui.MenuModuleBunkers;
import com.soraxus.prisons.bunkers.gui.tile.MenuTileEmpty;
import com.soraxus.prisons.bunkers.matchmaking.BunkerMatchMaker;
import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.shop.BunkerShop;
import com.soraxus.prisons.bunkers.tools.ToolUtils;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.bunkers.workers.Task;
import com.soraxus.prisons.bunkers.workers.TaskBuildAndEnable;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.event.bunkers.BunkerPlayerTeleportEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.string.TextUtil;
import lombok.Getter;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleBunkers extends CoreModule {
    public static final String WORLD_PREFIX = "bunker_";

    public static ModuleBunkers instance;

    public static boolean isBunkerWorld(String world) {
        return world.startsWith(WORLD_PREFIX);
    }

    public static Bunker byBukkitWorld(World world) {
        String worldName = world.getName();
        if (!worldName.startsWith(WORLD_PREFIX)) {
            return null;
        }
        String id = worldName.substring(WORLD_PREFIX.length());
        id = TextUtil.insertDashUUID(id);
        return BunkerManager.instance.getLoadedBunker(UUID.fromString(id));
    }

    public static void messageDevs(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.hasPermission("bunker.dev")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lBunkers > &c" + message));
            }
        });
    }

    @Override
    protected void onEnable() {
        new BunkerManager(new File(getDataFolder(), "bunkers"));
        new BunkerMatchMaker(BunkerManager.instance);
        instance = this;
        Scheduler.scheduleSyncRepeatingTaskT(() -> BunkerManager.instance.tick(), 1, 1);
        BunkerSchematics.clear();
    }

    @Override
    protected void onDisable() {
        Synchronizer.finishRunnables();
        BunkerSchematics.clear(); //In case this is a reload, release the cached schematics

        BunkerMatchMaker.instance.getReservedMatches().forEach(Match::end); //End Matches

        BunkerManager.instance.getLoadedBunkers().forEach((b) -> { //Unload bunkers
            try {
                BunkerManager.instance.saveAndUnloadBunkerSync(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        BunkerManager.instance.saveCachedRatings();
        BunkerManager.instance.loadingService.shutdown();
    }

    public Bunker fromWorld(World world) {
        String worldName = world.getName();
        if (worldName.startsWith(WORLD_PREFIX)) {

            //Get the bunker from the world
            String id = worldName.substring(WORLD_PREFIX.length());
            id = TextUtil.insertDashUUID(id);
            return BunkerManager.instance.getLoadedBunker(UUID.fromString(id));
        }
        return null;
    }

    @Override
    public String getName() {
        return "Bunkers";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.IRON_BLOCK).setName("&f&lBunkers")
                .addLore("&7Click to view &f&lBunkers").build())
                .setClickHandler((e, i) -> new MenuModuleBunkers(backButton).open(e.getWhoClicked()));
    }

    @Getter
    private final Map<UUID, BunkerElementType> selectedTypes = new ConcurrentHashMap<>();

    @EventSubscription
    public void onLeave(PlayerQuitEvent event) {
        selectedTypes.remove(event.getPlayer().getUniqueId());
    }

    @EventSubscription
    public void onClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            if (!e.getAction().equals(Action.RIGHT_CLICK_AIR))
                return;
            if (e.getItem() != null && ToolUtils.isBuildTool(e.getItem())) {
                BunkerShop shop = new BunkerSelectiveShop((t) -> selectedTypes.put(e.getPlayer().getUniqueId(), t));
                shop.getMenu(null).open(e.getPlayer());
            }
            return;
        }
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Location loc = e.getClickedBlock().getLocation();
        String worldName = loc.getWorld().getName();
        if (worldName.startsWith(WORLD_PREFIX)) {

            //Get the bunker from the world
            String id = worldName.substring(WORLD_PREFIX.length());
            id = TextUtil.insertDashUUID(id);
            Bunker bunker = BunkerManager.instance.getLoadedBunker(UUID.fromString(id));
            if (bunker != null && (bunker.getGang() == null || !bunker.getGang().isMember(e.getPlayer().getUniqueId()))) {
                e.setCancelled(true);
                return;
            }

            assert bunker != null;

            e.setCancelled(true);

            IntVector2D pos = bunker.getWorld().getTileAt(loc);
            if (bunker.getTileMap().isWithin(pos)) {

                //Get the tile
                Tile tile = bunker.getTileMap().getTile(pos);
                ItemStack stack = e.getItem();
                if (tile == null) {

                    //Tile is empty
                    if (stack != null && ToolUtils.isDefaultTool(stack)) {
                        new MenuTileEmpty(bunker, pos).open(e.getPlayer());
                    } else if (stack != null && ToolUtils.isBuildTool(stack)) {
                        BunkerElementType type = selectedTypes.get(e.getPlayer().getUniqueId());

                        if (!bunker.hasResources(type.getBuildCost(1))) {
                            bunker.messageMember(e.getPlayer(), "&cYou don't have enough resources to build this.");
                            return;
                        }
                        bunker.removeResources(type.getBuildCost(1));

                        BunkerElement element = type.getConstructor().createElement(bunker);

                        Worker worker = bunker.getFreeWorker();

                        if (worker == null) {
                            bunker.messageMember(e.getPlayer(), "&cNo available worker.");
                            return;
                        }

                        if (!bunker.setElement(pos, element)) {
                            bunker.messageMember(e.getPlayer(), "&cNo room for this building. This building has a size of &f" +
                                    element.getShape().getX() + " by " + element.getShape().getY());
                        }

                        tile = bunker.getTileMap().getTile(pos); //Tile is now non null

                        Task task = new TaskBuildAndEnable(tile, worker);
                        if (!task.start()) {
                            bunker.messageMember(e.getPlayer(), "&cNo available worker.");
                        }
                    }
                } else {

                    //Tile is not empty
                    //Check for protection
                    if (!tile.getParent().hasFlag(BunkerElementFlag.PROTECTED))
                        e.setCancelled(false);

                    //Call internal events
                    if (stack != null && ToolUtils.isDefaultTool(stack)) {
                        try {
                            tile.getParent().onClickTool(e);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        try {
                            tile.getParent().onClick(e);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @EventSubscription
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getMessage().contains("12395_%PRINT_THREADS%_12834")) {
            Map liveThreads = Thread.getAllStackTraces();
            for (Iterator i = liveThreads.keySet().iterator(); i.hasNext(); ) {
                Thread key = (Thread) i.next();
                System.err.println("Thread " + key.getName());
                StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
                for (int j = 0; j < trace.length; j++) {
                    System.err.println("\tat " + trace[j]);
                }
            }
        }
    }

    @EventSubscription
    public void onTeleport(PlayerTeleportEvent e) {
        Bunker bunkerFrom = byBukkitWorld(e.getFrom().getWorld());
        if (bunkerFrom != null) {
            bunkerFrom.setPreviousLocation(e.getPlayer().getUniqueId(), Vector3D.fromBukkitVector(e.getFrom().toVector()));
            return;
        }
        Bunker bunkerTo = byBukkitWorld(e.getTo().getWorld());
        if (bunkerTo != null) {
            bunkerTo.setPreviousLocation2(e.getPlayer().getUniqueId(), e.getFrom());
            BunkerPlayerTeleportEvent event = new BunkerPlayerTeleportEvent(e.getPlayer(), bunkerTo);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
}
