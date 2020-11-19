package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.util.EventSubscription;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class QuickSell extends AbstractCE {

    private Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList("Sell your inventory fast by shift clicking");
    }

    @Override
    public String getDisplayName() {
        return "&dQuick Sell";
    }

    @Override
    public long getCost(int level) {
        return 0;
    }

    @Override
    public String getName() {
        return "Quick Sell";
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
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }

    @EventSubscription
    private void onInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.LEFT_CLICK_AIR)
            return;
        Player player = event.getPlayer();
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if(hand == null)
            return;
        int level = getLevel(hand);
        if(level == 0)
            return;
        if (player.isSneaking()) {
            long cooldown = (long) (30000D - (28000D / 100D * level));
            if (!cooldowns.containsKey(player.getUniqueId()))
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() - cooldown);
            if (System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < cooldown) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Sell > " + ChatColor.RED + "You must wait " + Math.round((cooldown - (System.currentTimeMillis() - cooldowns.get(player
                        .getUniqueId()))) / 1000d) + " seconds before using this again!");
                return;
            }
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            ModuleSelling.instance.sellall(player);
        }
    }
}
