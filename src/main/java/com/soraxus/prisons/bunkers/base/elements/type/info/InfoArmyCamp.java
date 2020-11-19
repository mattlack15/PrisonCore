package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.util.items.ItemBuilder;
import org.bukkit.Material;

public class InfoArmyCamp implements BunkerElementTypeInfo {
    @Override
    public int getBuildTimeTicks(int level) {
        return level * 20 * 60 * 2;
    }

    @Override
    public Storage[] getBuildCost(int level) {
        return new Storage[0];
    }

    @Override
    public TypeShopInfo getShopInfo() {
        return new TypeShopInfo("Army").setItem(new ItemBuilder(Material.BED, 1)
                .setName("&eArmy Camp")
                .addLore("&7Even the toughest warriors needs sleep. Grav more than anybody.")
                .build());
    }
}
