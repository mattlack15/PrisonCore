package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Unbreaking extends AbstractCE {
    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public long getCost(int level) {
        return 0;
    }

    @Override
    public String getName() {
        return "Unbreaking";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }

    @Override
    public void onUnenchant(ItemStack stack) {
        stack.removeEnchantment(Enchantment.DURABILITY);
    }

    @Override
    public void onEnchant(ItemStack stack, int level) {
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        stack.addUnsafeEnchantment(Enchantment.DURABILITY, level);
    }
}
