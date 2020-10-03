package com.soraxus.prisons.bunkers.npc.info.types;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCTypeInfo;

import java.util.List;

public class NPCTypeBomber implements BunkerNPCTypeInfo {
    @Override
    public int getGenerationTimeTicks(int level) {
        return 0;
    }

    @Override
    public Storage[] getCost(int level) {
        return new Storage[0];
    }

    @Override
    public double getMaxHealth(int level) {
        return 0;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public List<String> getUpgradePerks(int currentLevel) {
        return null;
    }
}
