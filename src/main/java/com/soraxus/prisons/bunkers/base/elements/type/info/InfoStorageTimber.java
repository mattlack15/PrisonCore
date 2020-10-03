package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.util.ItemBuilder;
import org.bukkit.Material;

public class InfoStorageTimber implements BunkerElementTypeInfo {
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
        return new TypeShopInfo("Storage").setItem(new ItemBuilder(Material.LOG, 1).setName("&eTimber Storage")
                .addLore("&7Storage for your pieces of wood").build());
    }
}
