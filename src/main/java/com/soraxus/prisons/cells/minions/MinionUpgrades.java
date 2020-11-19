package com.soraxus.prisons.cells.minions;

import java.util.ArrayList;
import java.util.List;

public class MinionUpgrades {
    private static final List<Long> prices = new ArrayList<>();

    static {
        prices.add(1000L);
        prices.add(10000L);
        prices.add(500000L);
        prices.add(1000000L);
        prices.add(5000000L);
        prices.add(10000000L);
        prices.add(50000000L);
        prices.add(100000000L);
        prices.add(500000000L);
        prices.add(1000000000L);
    }

    public static long getPrice(int currentLevel) {
        currentLevel--;
        if (prices.size() <= currentLevel) {
            return -1L;
        }
        return prices.get(currentLevel);
    }
}
