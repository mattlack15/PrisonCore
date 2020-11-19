package com.soraxus.prisons.mines;

import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MineCreationSession {

    private static List<MineCreationSession> sessions = new ArrayList<>();
    private static ItemStack wand = new ItemBuilder(Material.IRON_AXE, 1).setName("&3&lMine Wand")
            .addLore("&7Use this exactly like a world-edit wand").build();
    private String name = null;
    private Vector3D point1;
    private Vector3D point2;
    private Consumer<Mine> callback;
    private UUID player;

    public MineCreationSession(Player player, Consumer<Mine> callback) {
        EventSubscriptions.instance.subscribe(this);
        this.player = player.getUniqueId();
        sessions.add(this);
        this.callback = callback;
        player.sendMessage(ModuleMines.instance.getPrefix() + "Please enter the name of the mine in chat:");
    }

    @EventSubscription
    private void onLeave(PlayerQuitEvent event) {
        sessions.remove(this);
        callback.accept(null);
    }

    @EventSubscription
    private void onClick(PlayerInteractEvent event) {
        if (!event.getPlayer().getUniqueId().equals(this.player))
            return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR))
            return;
        if (!event.getItem().isSimilar(wand))
            return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            this.point1 = Vector3D.fromBukkitVector(event.getClickedBlock().getLocation().toVector());
            event.getPlayer().sendMessage(ModuleMines.instance.getPrefix() + "Position 1 Set!");
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            this.point2 = Vector3D.fromBukkitVector(event.getClickedBlock().getLocation().toVector());
            event.getPlayer().sendMessage(ModuleMines.instance.getPrefix() + "Position 2 Set!");
        } else if (event.getAction().equals(Action.RIGHT_CLICK_AIR) && event.getPlayer().isSneaking()) {
            if (this.point1 != null && this.point2 != null && name != null) {
                Mine mine = new Mine(name, new CuboidRegion(event.getPlayer().getLocation().getWorld(), point1, point2));
                event.getPlayer().sendMessage(ModuleMines.instance.getPrefix() + ChatColor.GREEN + "Mine Created!");
                sessions.remove(this);
                callback.accept(mine);
                return;
            }
        }
        if (this.point1 != null && this.point2 != null && name != null)
            event.getPlayer().sendMessage(ModuleMines.instance.getPrefix() + ChatColor.GOLD + "Shift Right Click the air to finish!");
    }

    @EventSubscription(priority = EventPriority.NORMAL)
    private void onChat(AsyncPlayerChatEvent event) {
        if (name == null && event.getPlayer().getUniqueId().equals(this.player)) {
            event.setCancelled(true);
            name = event.getMessage();
            Synchronizer.synchronize(() -> {
                event.getPlayer().sendMessage(ModuleMines.instance.getPrefix() + "Use this wand to select the mine area");
                event.getPlayer().getInventory().addItem(wand);
            });
        }
    }
}
