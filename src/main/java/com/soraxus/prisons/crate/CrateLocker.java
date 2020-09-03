package com.soraxus.prisons.crate;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class CrateLocker {
    private Map<String, Integer> amounts;

    public CrateLocker() {
        this(new HashMap<>());
    }

    public CrateLocker(ItemStack item) {
    }
}