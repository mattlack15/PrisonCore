package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;

public class InfoArmyBarracks implements BunkerElementTypeInfo {
    @Override
    public int getBuildTimeTicks(int level) {
        return level * 20 * 60 * 5 * 0;
    }

    @Override
    public Storage[] getBuildCost(int level) {
        return new Storage[0];
    }
}
