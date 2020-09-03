package com.soraxus.prisons.bunkers.npc.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;

public interface BunkerNPCTypeInfo {
    int getGenerationTimeTicks(int level);
    Storage[] getCost(int level);
    int getMaxLevel();
}
