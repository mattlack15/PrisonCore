package com.soraxus.prisons.mines;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.mines.cmd.CmdRenameMine;
import com.soraxus.prisons.mines.cmd.CmdWarps;
import com.soraxus.prisons.mines.gui.MenuMines;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.ranks.PRankPlayer;
import com.soraxus.prisons.ranks.Rank;
import com.soraxus.prisons.ranks.RankupManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.display.ActionBar;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ModuleMines extends CoreModule {
    public static ModuleMines instance;
    int taskId = -1;
    private String prefix;
    private final Map<UUID, Boolean> allowedFlight = new HashMap<>();

    public String getName() {
        return "Mines";
    }

    protected void onEnable() {
        instance = this;
        this.createFiles(MineFiles.class);
        new MineManager();
        MineManager.instance.loadAll();
        MineManager.instance.getLoaded().forEach((m) -> {

            for (Mine mine : MineManager.instance.getLoaded()) {
                if (mine.getName().equalsIgnoreCase(m.getName()) && mine != m) {
                    MineManager.instance.remove(m.getName());
                }
            }

            if (m.getName().length() != 0 && Character.isLowerCase(m.getName().charAt(0))) {
                Mine.renameMine(m, MineManager.instance, m.getName().toUpperCase());
            }

        });
        this.prefix = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"));
        if (this.taskId == -1) {
            this.taskId = Scheduler.scheduleSyncRepeatingTaskT(this::update, 0, 5);
        }

        new CmdWarps().register();
        new CmdRenameMine().register();
    }

    protected void onDisable() {
        MineManager.instance.getLoaded().forEach((m) -> {
            try {
                MineManager.instance.unload(m.getName()).get();
            } catch (ExecutionException | InterruptedException var2) {
                var2.printStackTrace();
            }

        });
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }

    }

    public MenuElement getGUI(MenuElement back) {
        return (new MenuElement((new ItemBuilder(Material.DIAMOND_PICKAXE, 1)).setName("&f&lMines").addLore("&7Click to enter &f&lMines").build())).setClickHandler((e, i) -> {
            Player player = (Player)e.getWhoClicked();
            Mine mine = MineManager.instance.getMineOf(player.getLocation());
            if (mine == null) {
                mine = MineManager.instance.getMineOf(player.getLocation().clone().add(0.0D, -5.0D, 0.0D));
            }

            (new MenuMines(back, mine)).open(player, new Object[0]);
        });
    }

    public String getPrefix() {
        return this.prefix;
    }

    public double getMinedThreshold() {
        return this.getConfig().getDouble("reset-threshold-percentage-mined");
    }

    public boolean isFlightAllowedNearMine() {
        return this.getConfig().getBoolean("enable-flight-near-mines");
    }

    private void update() {
        this.sendActionBars();
        MineManager.instance.getLoaded().forEach((m) -> {
            if (System.currentTimeMillis() - m.getLastMinedBlock().get() > 1200000L) {
                Rank rank = RankupManager.instance.getRank(m.getName());
                if (rank == null) {
                    MineManager.instance.remove(m.getName());
                    return;
                }

                Vector3D p1 = m.getRegion().getMaximumPoint();
                if (m.getRegion().getWorld().isChunkLoaded(p1.getBlockX() >> 4, p1.getBlockZ() >> 4)) {
                    m.getLastMinedBlock().set(System.currentTimeMillis());
                    m.reset();
                } else {
                    m.getLastMinedBlock().getAndAdd(10000L);
                }
            }

        });
    }

    @EventSubscription(
            priority = EventPriority.LOW
    )
    private void onMine(BlockBreakEvent event) {
        Mine mine = MineManager.instance.getMineOf(event.getBlock().getLocation());
        if (mine != null) {
            mine.getLastMinedBlock().set(System.currentTimeMillis());
            PRankPlayer pRankPlayer = RankupManager.instance.getPlayer(event.getPlayer().getUniqueId());
            if ((pRankPlayer != null && pRankPlayer.getRankIndex() < mine.getOrder() || mine.getPermission() != null) && (mine.getPermission() == null || !event.getPlayer().hasPermission(mine.getPermission()))) {
                event.getPlayer().sendMessage(this.getPrefix() + ChatColor.RED + "Oof! You cannot mine in this mine.");
                event.setCancelled(true);
            } else if (mine.isResetting()) {
                event.getPlayer().sendMessage(this.getPrefix() + ChatColor.RED + "This mine is resetting, patience is key :D");
                event.setCancelled(true);
            } else {
                mine.incrementBlocksMined(1);
                MineManager.instance.queueSaveMineOperation(mine);
            }
        }
    }

    private String firstLetterUpper(String s) {
        if (s.length() < 1) {
            return s;
        } else {
            StringBuilder builder = new StringBuilder(s);
            builder.setCharAt(0, Character.toUpperCase(s.charAt(0)));
            return builder.toString();
        }
    }

    private void sendActionBars() {
        Bukkit.getOnlinePlayers().forEach((p) -> {
            Mine mine = MineManager.instance.getMineOf(p.getLocation());
            if (mine != null) {
                double percentage = (double)mine.getBlocksMined() / (double)mine.getMineArea();
                percentage = (double)((float)Math.round(percentage * 1000.0D) / 1000.0F);
                StringBuilder builder = new StringBuilder();
                builder.append(ChatColor.AQUA).append(this.firstLetterUpper(mine.getName())).append(" ");
                builder.append(ChatColor.GREEN).append(ChatColor.BOLD);

                for(int i = 0; i < 10; ++i) {
                    builder.append("█");
                    if ((long)i == Math.round(percentage * 10.0D)) {
                        builder.append(ChatColor.GRAY).append(ChatColor.BOLD);
                    }
                }

                builder.append(ChatColor.YELLOW).append(" ").append((float)Math.round(percentage * 1000.0D) / 10.0F).append("%");
                ActionBar.send(p, builder.toString());
            }

        });
    }

    @EventSubscription
    private void onMove(PlayerMoveEvent event) {
        if (this.isFlightAllowedNearMine()) {
            Location p = event.getPlayer().getLocation();
            boolean isClose = false;
            Iterator var4 = MineManager.instance.getLoaded().iterator();

            while(var4.hasNext()) {
                Mine mines = (Mine)var4.next();
                CuboidRegion region = mines.getRegion();
                if (region.smallestDistance(Vector3D.fromBukkitVector(p.toVector())) <= 8.0D && event.getPlayer().hasPermission("spc.mines.flight")) {
                    if (!this.allowedFlight.containsKey(event.getPlayer().getUniqueId())) {
                        this.allowedFlight.put(event.getPlayer().getUniqueId(), event.getPlayer().getAllowFlight());
                    }

                    if (!event.getPlayer().getAllowFlight()) {
                        event.getPlayer().setAllowFlight(true);
                    }

                    isClose = true;
                    break;
                }
            }

            if (!isClose && this.allowedFlight.containsKey(event.getPlayer().getUniqueId())) {
                event.getPlayer().setAllowFlight((Boolean)this.allowedFlight.remove(event.getPlayer().getUniqueId()));
            }
        }

    }
}
