package com.soraxus.prisons;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.menus.MenuElement;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ModuleBreak extends CoreModule {

    public static ModuleBreak instance;

    public ModuleBreak() {
        instance = this;
    }

    @Override
    public String getName() {
        return "Break-Module";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @EventSubscription(priority = EventPriority.HIGHEST)
    private void onBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        this.onBreak(event.getPlayer(), Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector()), event.getBlock().getData() << 12 | event.getBlock().getTypeId());
        event.setDropItems(false);
        event.setExpToDrop(0);
    }

    public synchronized void onBreak(Player player, Vector3D location, int combinedId) {
        onBreak(player, location, combinedId, 1);
    }

    public synchronized void onBreak(Player player, Vector3D location, int combinedId, int amount) {
        PrisonBlockBreakEvent event = new PrisonBlockBreakEvent(player, location, combinedId);
        event.setAmount(amount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getAmount() > 0) {
            player.getInventory().addItem(new ItemStack(combinedId & 4095, event.getAmount(), (short) 0, (byte) (combinedId >> 12)));
            if (player.getInventory().firstEmpty() == -1)
                player.sendTitle(ChatColor.RED + "Your Inventory is Full!", "", 0, 60, 10);
        }
    }
}
