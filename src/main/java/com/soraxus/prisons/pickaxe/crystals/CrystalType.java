package com.soraxus.prisons.pickaxe.crystals;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum CrystalType {
    TOKEN("Token Booster"),
    SELL("Sell Booster"),
    XP("XP Booster"),
    STAR("Star Booster");

    private String displayName;
}
