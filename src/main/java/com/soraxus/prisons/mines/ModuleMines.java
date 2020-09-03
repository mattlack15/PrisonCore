package com.soraxus.prisons.mines;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.mines.gui.MenuMines;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.display.ActionBar;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.menus.MenuElement;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ModuleMines extends CoreModule {

    public static ModuleMines instance;
    int taskId = -1;
    private String prefix;
    private Map<UUID, Boolean> allowedFlight = new HashMap<>();

    @Override
    public String getName() {
        return "Mines";
    }

    @Override
    protected void onEnable() {
        instance = this;
        createFiles(MineFiles.class);
        new MineManager();
        MineManager.instance.loadAll();
        prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix"));
        if (taskId == -1)
            taskId = Scheduler.scheduleSyncRepeatingTaskT(this::sendActionBars, 0, 5);
    }

    @Override
    protected void onDisable() {
        MineManager.instance.getLoaded().forEach(m -> {
            try {
                MineManager.instance.unload(m.getName()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    @Override
    public MenuElement getGUI(MenuElement back) {
        return new MenuElement(new ItemBuilder(Material.DIAMOND_PICKAXE, 1).setName("&f&lMines").addLore("&7Click to enter &f&lMines").build())
                .setClickHandler((e, i) -> {
                    new MenuMines(back).open((Player) e.getWhoClicked());
                });
    }

    public String getPrefix() {
        return prefix;
    }

    public double getMinedThreshold() {
        return getConfig().getDouble("reset-threshold-percentage-mined");
    }

    public boolean isFlightAllowedNearMine() {
        return getConfig().getBoolean("enable-flight-near-mines");
    }

    //Events
    @EventSubscription
    private void onMine(BlockBreakEvent event) {
        Mine mine = MineManager.instance.getMineOf(event.getBlock().getLocation());
        if (mine == null)
            return;

        if (mine.getPermission() != null && !event.getPlayer().hasPermission(mine.getPermission())) {
            event.getPlayer().sendMessage(getPrefix() + ChatColor.RED + "Oof! You cannot mine in this mine.");
            event.setCancelled(true);
            return;
        }

        if (mine.isResetting()) {
            event.getPlayer().sendMessage(getPrefix() + ChatColor.RED + "This mine is resetting, patience is key :D");
            event.setCancelled(true);
            return;
        }

        mine.incrementBlocksMined(1);
        MineManager.instance.queueSaveMineOperation(mine);
    }

    private void sendActionBars() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            Mine mine = MineManager.instance.getMineOf(p.getLocation());
            if (mine != null) {
                double percentage = mine.getBlocksMined() / (double) mine.getMineArea();
                percentage = Math.round(percentage * 1000d) / 1000f;
                StringBuilder builder = new StringBuilder();
                builder.append("Mine ").append(mine.getName()).append(" ");
                builder.append(ChatColor.GREEN).append(ChatColor.BOLD);
                for (int i = 0; i < 10; i++) {
                    builder.append("█");
                    if (i == Math.round(percentage * 10f)) {
                        builder.append(ChatColor.GRAY).append(ChatColor.BOLD);
                    }
                }
                builder.append(ChatColor.YELLOW).append(" ").append(Math.round(percentage * 1000f) / 10f).append("%");
                ActionBar.send(p, builder.toString());
            }
        });
    }

    @EventSubscription
    private void onMove(PlayerMoveEvent event) {
        if (isFlightAllowedNearMine()) {
            Location p = event.getPlayer().getLocation();
            boolean isClose = false;
            for (Mine mines : MineManager.instance.getLoaded()) {
                CuboidRegion region = mines.getRegion();
                if (region.smallestDistance(Vector3D.fromBukkitVector(p.toVector())) <= 8) {
                    if (!allowedFlight.containsKey(event.getPlayer().getUniqueId())) {
                        allowedFlight.put(event.getPlayer().getUniqueId(), event.getPlayer().getAllowFlight());
                    }
                    if (!event.getPlayer().getAllowFlight())
                        event.getPlayer().setAllowFlight(true);
                    isClose = true;
                    break;
                }
            }
            if(!isClose)
                if (allowedFlight.containsKey(event.getPlayer().getUniqueId()))
                    event.getPlayer().setAllowFlight(allowedFlight.remove(event.getPlayer().getUniqueId()));
        }
    }
}
