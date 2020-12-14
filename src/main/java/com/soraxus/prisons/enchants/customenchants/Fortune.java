package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class Fortune extends AbstractCE {
    @Override
    protected void onEnable() {

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
        return "Fortune";
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
        if (hasEnchant(event.getPlayer().getInventory().getItemInMainHand())) {
            if((event.getBlock() & 0xFFF) == Material.SPONGE.getId()) //If it is a lucky block
                return;
            event.setAmount(event.getAmount() * (getInfo(event.getPlayer().getInventory().getItemInMainHand()).getEnchants().get(this) + 1));
        }
    }

    @Override
    public void onUnenchant(ItemStack stack) {

    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }
}
