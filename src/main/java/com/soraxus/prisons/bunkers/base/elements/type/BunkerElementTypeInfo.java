package com.soraxus.prisons.bunkers.base.elements.type;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;

public interface BunkerElementTypeInfo {
    int getBuildTimeTicks(int level);
    Storage[] getBuildCost(int level);
    TypeShopInfo getShopInfo();
}
