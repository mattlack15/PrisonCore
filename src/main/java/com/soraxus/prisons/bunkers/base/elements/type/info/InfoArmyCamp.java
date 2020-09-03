package com.soraxus.prisons.bunkers.base.elements.type.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementTypeInfo;

public class InfoArmyCamp implements BunkerElementTypeInfo {
    @Override
    public int getBuildTimeTicks(int level) {
        return level * 20 * 60 * 2;
    }

    @Override
    public Storage[] getBuildCost(int level) {
        return new Storage[0];
    }
}
