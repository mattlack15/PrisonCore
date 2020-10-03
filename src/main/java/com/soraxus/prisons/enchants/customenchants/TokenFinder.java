package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.math.MathUtils;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class TokenFinder extends AbstractCE {
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
        return "Token Finder";
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

    @EventSubscription
    private void onBreak(PrisonBlockBreakEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (hasEnchant(item)) {
            int level = getLevel(item);
            double chance = 0.01 + percentIncrease * level;
            if (MathUtils.isRandom(chance, 100.0)) {
                long amount = level * 2;
                Economy.tokens.addBalance(event.getPlayer().getUniqueId(), amount);
                event.getPlayer().sendMessage("§a+§e" + amount + " Tokens");
            }
        }
    }

    @Override
    public void onUnenchant(ItemStack stack) {

    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }
}
