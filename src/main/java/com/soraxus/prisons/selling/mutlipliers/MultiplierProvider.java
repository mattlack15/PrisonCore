package com.soraxus.prisons.selling.mutlipliers;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface MultiplierProvider {
    double getMultipler(Player player);
}
