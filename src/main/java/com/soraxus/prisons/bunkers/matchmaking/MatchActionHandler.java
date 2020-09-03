package com.soraxus.prisons.bunkers.matchmaking;

import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class MatchActionHandler {

    private Match parent;

    public MatchActionHandler(Match parent) {
        this.parent = parent;
        EventSubscriptions.instance.subscribe(this);
    }

    public void setupPlayer(Player player) {
        player.getInventory().setItem(1, getSpawnWand());
        player.getInventory().setItem(7, getEnd());
    }

    public static boolean check(ItemStack stack, Player player) {
        return stack.isSimilar(player.getInventory().getItemInMainHand());
    }

    public static ItemStack getSpawnWand() {
        ItemBuilder builder = new ItemBuilder(Material.IRON_HOE, 1);
        builder.setName("&f&lSpawn Wand")
                .addLore("",
                        "&7Use this to spawn your fiercest soldiers",
                        "&7on the battlefield!",
                        "",
                        "&fLeft Click - &aSpawn",
                        "&fRight Click - &aSelect Type");
        builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return builder.build();
    }

    public static ItemStack getEnd() {
        return new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&c&lEnd")
                .addLore("&7Click to chicken out of this attack")
                .addLore("&8JK you're not a chicken").build();
    }

    @EventSubscription
    private void onInteract(PlayerInteractEvent event) {

    }
}
