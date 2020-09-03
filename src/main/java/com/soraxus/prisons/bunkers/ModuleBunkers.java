package com.soraxus.prisons.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.gui.tile.MenuTileEmpty;
import com.soraxus.prisons.bunkers.matchmaking.BunkerMatchMaker;
import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.tools.ToolUtils;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.string.TextUtil;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

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
        BunkerMatchMaker.instance.getReservedMatches().forEach(Match::end); //End Matches

        BunkerManager.instance.getLoadedBunkers().forEach((b) -> { //Unload bunkers
            try {
                BunkerManager.instance.saveAndUnloadBunkerSync(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        BunkerManager.instance.lastTick = -2; //Make sure that the crash detector doesn't think that the server has crashed
        BunkerManager.instance.saveCachedRatings();
        BunkerSchematics.clear(); //In case this is a reload, release the cached schematics
    }

    @Override
    public String getName() {
        return "Bunkers";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @EventSubscription
    public void onClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Location loc = e.getClickedBlock().getLocation();
        String worldName = loc.getWorld().getName();
        if (worldName.startsWith(WORLD_PREFIX)) {
            String id = worldName.substring(WORLD_PREFIX.length());
            id = TextUtil.insertDashUUID(id);
            Bunker bunker = BunkerManager.instance.getLoadedBunker(UUID.fromString(id));
            IntVector2D pos = bunker.getWorld().getTileAt(loc);
            if (bunker.getTileMap().isWithin(pos)) {
                Tile tile = bunker.getTileMap().getTile(pos);
                ItemStack stack = e.getItem();
                if (tile == null) {
                    if (stack != null && ToolUtils.isDefaultTool(stack)) {
                        new MenuTileEmpty(bunker, pos).open(e.getPlayer());
                    }
                } else {
                    if (stack != null && ToolUtils.isDefaultTool(stack)) {
                        tile.getParent().onClickTool(e);
                    } else {
                        tile.getParent().onClick(e);
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
        }
    }
}
