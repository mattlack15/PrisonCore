package com.soraxus.prisons.bunkers.matchmaking.stats;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class MatchStats implements GravSerializable {
    private List<Storage> collectedResources = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger deaths = new AtomicInteger();
    private final AtomicInteger spawns = new AtomicInteger();
    private final AtomicInteger destroyedBuildings = new AtomicInteger();
    private final AtomicInteger kills = new AtomicInteger();
    private final AtomicInteger matchDurationSeconds = new AtomicInteger();
    private final AtomicBoolean attackerSuccess = new AtomicBoolean(false);
    private final AtomicLong startTime = new AtomicLong();

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

    public MatchStats() {
    }

    public MatchStats(GravSerializer serializer) {
        Meta meta = new Meta(serializer);

        this.collectedResources = Collections.synchronizedList(meta.get("collectedResources"));
        this.deaths.set(meta.get("deaths"));
        this.spawns.set(meta.get("spawns"));
        this.destroyedBuildings.set(meta.get("destroyedBuildings"));
        this.kills.set(meta.get("kills"));
        this.matchDurationSeconds.set(meta.get("matchDurationSeconds"));
        this.attackerSuccess.set(meta.get("attackerSuccess"));
        this.startTime.set(meta.get("startTime"));
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        Meta meta = new Meta();

        meta.set("collectedResources", collectedResources);
        meta.set("deaths", deaths.get());
        meta.set("spawns", spawns.get());
        meta.set("destroyedBuildings", destroyedBuildings.get());
        meta.set("kills", kills.get());
        meta.set("matchDurationSeconds", matchDurationSeconds.get());
        meta.set("attackerSuccess", attackerSuccess.get());
        meta.set("startTime", startTime.get());

        meta.serialize(gravSerializer);
    }
}
