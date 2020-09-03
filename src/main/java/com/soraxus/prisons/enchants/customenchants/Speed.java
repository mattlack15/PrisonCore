package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.api.enchant.EnchantInfo;
import com.soraxus.prisons.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed extends AbstractCE {
    int taskId = -1;

    @Override
    protected void onEnable() {
        if(taskId == -1)
            taskId = Scheduler.scheduleSyncRepeatingTaskT(this::update, 0, 10);
    }

    @Override
    protected void onDisable() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    private void update() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            if(hasEnchant(p.getInventory().getItemInMainHand())) {
                EnchantInfo info = getInfo(p.getInventory().getItemInMainHand());
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, info.getEnchants().get(this)-1, false, false), true);
            }
        });
    }

    @Override
    public long getCost(int level) {
        return 0;
    }

    @Override
    public String getName() {
        return "Speed";
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
