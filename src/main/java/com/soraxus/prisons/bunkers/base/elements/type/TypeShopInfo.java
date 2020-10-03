package com.soraxus.prisons.bunkers.base.elements.type;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class TypeShopInfo {

    private final String section;
    private ItemStack item = null;

    public TypeShopInfo(String section) {
        this.section = section;
    }
    public TypeShopInfo setItem(ItemStack stack) {
        this.item = stack;
        return this;
    }
}
