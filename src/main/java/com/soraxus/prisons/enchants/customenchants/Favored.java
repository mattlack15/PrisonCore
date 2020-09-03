package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class Favored extends AbstractCE {
    private double percentIncrease;

    @Override
    protected void onEnable() {
        percentIncrease = getConfig().getDouble("percent-increase-per-level") / 100d;
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public long getCost(int level) {
        return (long) (100 * Math.pow(level, 2));
    }

    @Override
    public String getName() {
        return "Favored";
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

    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }
}
