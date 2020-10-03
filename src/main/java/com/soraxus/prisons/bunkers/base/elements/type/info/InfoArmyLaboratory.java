package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.util.ItemBuilder;
import org.bukkit.Material;

public class InfoArmyLaboratory implements BunkerElementTypeInfo {
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
        return new TypeShopInfo("Army").setItem(new ItemBuilder(Material.BREWING_STAND_ITEM, 1)
                .setName("&eLaboratory")
                .addLore("&7Do mad science on your warriors, make them work &e\uD834\uDD58\uD834\uDD65\uD834\uDD6E&7harder, better, faster, stronger&e\uD834\uDD58\uD834\uDD65\uD834\uDD6E&7.")
                .build());
    }
}
