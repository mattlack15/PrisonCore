package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class TokenRain extends AbstractCE {
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
        return "Token Rain";
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
            double chance = 0.001 + percentIncrease * level; // TODO: Balance this
            if (MathUtils.isRandom(chance, 100.0)) {
                long amount = level * 10; // TODO: Balance this
                Economy.tokens.addBalance(event.getPlayer().getUniqueId(), amount);
                for (int i = 0; i < 10; i ++) {
                    ItemStack it = new ItemBuilder(Material.DOUBLE_PLANT, 1)
                            .setName("displayItem" + i)
                            .build();
                    Vector3D vec = event.getLocation();
                    World world = event.getPlayer().getWorld();
                    Item itEnt = world
                            .dropItemNaturally(new Location(world, vec.getX(), vec.getY(), vec.getZ()), it);
                    itEnt.setVelocity(new org.bukkit.util.Vector(MathUtils.random(-1, 1), 2, MathUtils.random(-1, 1)));
                    itEnt.setCustomName("#nopickup#");
                    itEnt.setPickupDelay(100000);
                    itEnt.setCustomNameVisible(false); // Prevent people from seeing name
                    itEnt.setInvulnerable(true); // Avoid getting blown up, burning, etc.
                }
            }
        }
    }

    @EventSubscription
    public void onPickup(EntityPickupItemEvent e) {
        String str = e.getItem().getCustomName();
        if (str == null) {
            return;
        }
        if (str.contains("#nopickup#")) {
            e.setCancelled(true);
        }
    }

    @Override
    public void onUnenchant(ItemStack stack) {

    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }
}
