package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.math.MathUtils;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class CrateFinder extends AbstractCE {
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
        return "Crate Finder";
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
            double chance = 0.01 + percentIncrease * level; // TODO: Balance this
            if (MathUtils.isRandom(chance, 100.0)) {
                String crateType = "null";
                Crate crate = CrateManager.instance.get(crateType);
                if (crate == null) {
                    return;
                }
                event.getPlayer().getInventory().addItem(crate.getItem());
                event.getPlayer().sendMessage("§a+1 " + crate.getDisplayName());
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
