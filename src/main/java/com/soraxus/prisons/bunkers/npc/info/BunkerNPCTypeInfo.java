package com.soraxus.prisons.bunkers.npc.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;

import java.util.List;

public interface BunkerNPCTypeInfo {
    int getGenerationTimeTicks(int level);
    Storage[] getCost(int level);
    double getMaxHealth(int level);
    int getMaxLevel();
    List<String> getUpgradePerks(int currentLevel);
}
