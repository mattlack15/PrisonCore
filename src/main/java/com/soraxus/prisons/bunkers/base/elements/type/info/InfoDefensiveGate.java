package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.util.items.ItemBuilder;
import org.bukkit.Material;

public class InfoDefensiveGate implements BunkerElementTypeInfo {
    @Override
    public int getBuildTimeTicks(int level) {
        return 0;
    }

    @Override
    public Storage[] getBuildCost(int level) {
        return new Storage[0];
    }

    @Override
    public TypeShopInfo getShopInfo() {
        return new TypeShopInfo("Defense").setItem(new ItemBuilder(Material.FENCE_GATE, 1, (byte) 1)
                .setName("&eGate")
                .addLore("&7Block your enemies out! But with a gate!")
                .build());
    }
}
