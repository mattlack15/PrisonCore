package com.soraxus.prisons.util.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemUtils {
    /**
     * Create an displayItem from a block integer (type + data)
     *
     * @param block  Block data
     * @param amount Amount
     * @return Item
     */
    public static ItemStack fromBlock(int block, int amount) {
        return new ItemStack(block & 4095, amount, (short) 0, (byte) (block >> 12));
    }

    public static boolean isType(ItemStack item, String type) {
        String str = NBTUtils.instance.getString(item, "type");
        return (str != null && str.equals(type));
    }

    @NotNull
    public static ItemStack setType(ItemStack item, String type) {
        return NBTUtils.instance.setString(item, "type", type);
    }

    public static boolean decrementHand(Player player) {
        return decrementHand(player, 1);
    }
    public static boolean decrementHand(Player player, int amount) {
        if (player.getInventory().getItemInMainHand() == null) {
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        item.setAmount(item.getAmount() - amount);
        player.getInventory().setItemInMainHand(item);
        return true;
    }
}
