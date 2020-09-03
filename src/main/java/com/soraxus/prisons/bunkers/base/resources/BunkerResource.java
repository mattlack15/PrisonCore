package com.soraxus.prisons.bunkers.base.resources;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum BunkerResource {
    TIMBER("§6", "Timber"), // TODO Brown color code?
    STONE("§7", "Stone"),
    GOLD("§e", "Gold"),
    SAPPHIRE("§5", "Sapphire"),
    TBD("", "");


    private final String color;
    private final String displayName;

    public String fullDisplay() {
        return getColor() + getDisplayName();
    }
}
