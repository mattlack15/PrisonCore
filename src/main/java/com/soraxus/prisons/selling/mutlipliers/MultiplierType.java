package com.soraxus.prisons.selling.mutlipliers;

import com.soraxus.prisons.pickaxe.crystals.CrystalInfo;
import com.soraxus.prisons.pickaxe.crystals.CrystalManager;
import com.soraxus.prisons.pickaxe.crystals.CrystalType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public enum MultiplierType { // Everything that multiplies money from selling should go here
    SELL(p -> MultiplierManager.instance.getMultiplier(p.getUniqueId())),
    CRYSTAL(p -> {
        CrystalInfo info = CrystalManager.getInfo(p.getInventory().getItemInMainHand());
        if(info.totalPercent().containsKey(CrystalType.SELL)) {
            return info.totalPercent().get(CrystalType.SELL) / 100D;
        } else {
            return 1D;
        }
    });

    private MultiplierProvider provider;
    MultiplierType(MultiplierProvider provider) {
        this.provider = provider;
    }

    public double getMultiplier(Player player) {
        return provider.getMultipler(player);
    }

    private static List<MultiplierType> values; // Avoids excessive calls to the values() method
    public static List<MultiplierType> getValues() {
        if (values == null) {
            values = Arrays.asList(values());
        }
        return values;
    }
}
