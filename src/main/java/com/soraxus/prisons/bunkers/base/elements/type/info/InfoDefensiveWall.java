package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.util.ItemBuilder;
import org.bukkit.Material;

public class InfoDefensiveWall implements BunkerElementTypeInfo {
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
        return new TypeShopInfo("Defense").setItem(new ItemBuilder(Material.COBBLE_WALL, 1)
                .setName("&eWall")
                .addLore("&7Block your enemies out!")
                .build());
    }
}
