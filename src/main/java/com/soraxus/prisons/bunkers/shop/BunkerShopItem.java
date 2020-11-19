package com.soraxus.prisons.bunkers.shop;

import com.soraxus.prisons.util.items.ItemBuilder;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

//Immutable

@Getter
public class BunkerShopItem {
    private final ItemStack itemStack;
    private final BunkerShop parent;
    private final Supplier<Boolean> requirement;
    private final Consumer<Player> giver;

    public BunkerShopItem(BunkerShop parent, ItemStack item, Supplier<Boolean> requirement, Consumer<Player> giver, String... displayCost) {
        this.parent = parent;
        this.requirement = requirement;
        this.giver = giver;
        ItemBuilder builder = new ItemBuilder(item.clone());
        builder.addLore("");
        for (String cost : displayCost) {
            builder.addLore(cost);
        }
        this.itemStack = builder.build();
    }
}
