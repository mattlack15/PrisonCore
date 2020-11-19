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

    /**
     * Check whether the type NBT tag matches
     *
     * @param item Item
     * @param type Wanted type
     * @return {@code true} if the type matches
     */
    public static boolean isType(ItemStack item, String type) {
        String str = NBTUtils.instance.getString(item, "type");
        return (str != null && str.equals(type));
    }

    /**
     * Set the type NBT tag on an item
     *
     * @param item Item
     * @param type Type
     * @return New item
     */
    @NotNull
    public static ItemStack setType(ItemStack item, String type) {
        return NBTUtils.instance.setString(item, "type", type);
    }

    /**
     * Decrement the amount of the item in the player's hand by 1
     *
     * @param player Player
     * @return {@code true} if the player was holding an item
     */
    public static boolean decrementHand(Player player) {
        return decrementHand(player, 1);
    }

    /**
     * Decrement the amount of the item in the player's hand by amount
     *
     * @param player Player
     * @param amount Amount
     * @return {@code true} if the player was holding an item
     */
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
