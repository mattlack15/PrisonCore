package com.soraxus.prisons.selling.mutlipliers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MultiplierInfo {
    private Map<MultiplierType, Double> multipliers = new HashMap<>();

    public MultiplierInfo(Player player) {
        MultiplierType.getValues().forEach(type -> multipliers.put(type, type.getMultiplier(player)));
    }

    public double getTotal() {
        return multipliers.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum() + 1;
    }
}
