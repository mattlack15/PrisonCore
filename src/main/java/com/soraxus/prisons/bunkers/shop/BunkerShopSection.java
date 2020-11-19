package com.soraxus.prisons.bunkers.shop;

import com.soraxus.prisons.util.items.ItemBuilder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BunkerShopSection {
    @Getter
    private final String name;
    private final List<BunkerShopItem> itemList = new ArrayList<>();
    @Getter
    private final ItemStack displayItem;

    public BunkerShopSection(String name, ItemStack displayItem) {
        this.name = name;
        this.displayItem = new ItemBuilder(displayItem.clone()).addLore("", "&8Shop Section").build();
    }

    public synchronized void addItem(BunkerShopItem item) {
        this.itemList.add(item);
    }

    public synchronized List<BunkerShopItem> getItems() {
        return new ArrayList<>(itemList);
    }
}
