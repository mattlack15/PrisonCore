package com.soraxus.prisons.bunkers.matchmaking.stats;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.util.list.LockingList;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchStats {
    @Getter
    private final List<Storage> collectedResources = new LockingList<>();
    private final AtomicInteger deaths = new AtomicInteger();
    private final AtomicInteger spawns = new AtomicInteger();
    private final AtomicInteger destroyedBuildings = new AtomicInteger();
    private final AtomicInteger kills = new AtomicInteger();

    public void recordCollectedResource(Storage storage) {
        collectedResources.add(storage);
    }

    public void recordDeath() {
        deaths.incrementAndGet();
    }

    public void recordSpawn() {
        spawns.incrementAndGet();
    }

    public void recordDestroyedBuilding() {
        destroyedBuildings.incrementAndGet();
    }

    public void recordKill() {
        kills.incrementAndGet();
    }
}
