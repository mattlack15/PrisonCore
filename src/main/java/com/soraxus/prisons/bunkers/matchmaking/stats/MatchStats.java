package com.soraxus.prisons.bunkers.matchmaking.stats;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class MatchStats implements GravSerializable {
    private List<Storage> collectedResources = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger deaths = new AtomicInteger();
    private final AtomicInteger spawns = new AtomicInteger();
    private final AtomicInteger destroyedBuildings = new AtomicInteger();
    private final AtomicInteger kills = new AtomicInteger();
    private final AtomicInteger matchDurationSeconds = new AtomicInteger();
    private final AtomicBoolean attackerSuccess = new AtomicBoolean(false);

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

    public void setMatchDurationSeconds(int seconds) {
        this.matchDurationSeconds.set(seconds);
    }

    public int getMatchDurationSeconds() {
        return this.matchDurationSeconds.get();
    }

    public void setAttackerSuccess(boolean value) {
        this.attackerSuccess.set(value);
    }

    public boolean getAttackerSuccess() {
        return this.attackerSuccess.get();
    }

    public MatchStats() {}

    public MatchStats(GravSerializer serializer) {
        this.collectedResources = serializer.readObject();
        this.deaths.set(serializer.readInt());
        this.spawns.set(serializer.readInt());
        this.destroyedBuildings.set(serializer.readInt());
        this.kills.set(serializer.readInt());
        this.matchDurationSeconds.set(serializer.readInt());
        this.attackerSuccess.set(serializer.readBoolean());
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeObject(collectedResources);
        gravSerializer.writeInt(deaths.get());
        gravSerializer.writeInt(spawns.get());
        gravSerializer.writeInt(destroyedBuildings.get());
        gravSerializer.writeInt(kills.get());
        gravSerializer.writeInt(matchDurationSeconds.get());
        gravSerializer.writeBoolean(this.attackerSuccess.get());
    }
}
