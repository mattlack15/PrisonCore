package com.soraxus.prisons.pickaxe.crystals;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum CrystalType {
    TOKEN("Token Booster", new String[]{"Get more tokens from mining!"}),
    SELL("Sell Booster", new String[]{"Sell your blocks at a higher price!"}),
    XP("XP Booster", new String[]{"Level up your pickaxe faster!"}),
    STAR("Star Booster", new String[]{"Get more stars from mining!"});

    private String displayName;
    private String[] description;
}
