package com.soraxus.prisons.bunkers.base.resources;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum BunkerResource {
    TIMBER("§6", "Timber"),
    STONE("§7", "Stone"),
    GOLD("§e", "Gold"),
    NETHEREM("§5", "Netherem"),
    WATER("§9", "Water"),
    FOOD("§d", "Food");


    private final String color;
    private final String displayName;

    public String fullDisplay() {
        return getColor() + getDisplayName();
    }
}
