package com.soraxus.prisons.bunkers.npc.info.types;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCTypeInfo;

public class NPCTypeArcher implements BunkerNPCTypeInfo {
    @Override
    public int getGenerationTimeTicks(int level) {
        return 0;
    }

    @Override
    public Storage[] getCost(int level) {
        return new Storage[] {
                new Storage(BunkerResource.TIMBER, 0, 0),
                new Storage(BunkerResource.STONE, 0, 0),
        };
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }
}
