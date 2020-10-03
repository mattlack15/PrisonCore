package com.soraxus.prisons.bunkers.matchmaking;

import com.soraxus.prisons.bunkers.matchmaking.stats.MatchStats;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

@Getter
public class MatchInfo implements GravSerializable {
    private final String defendingName;
    private final UUID defendingBunker;
    private final String attackingName;
    private final UUID attackingBunker;
    private final MatchStats matchStats;

    public MatchInfo(String defendingName, UUID defendingBunker, String attackingName, UUID attackingBunker, MatchStats matchStats) {
        this.defendingName = defendingName;
        this.defendingBunker = defendingBunker;
        this.attackingName = attackingName;
        this.attackingBunker = attackingBunker;
        this.matchStats = matchStats;
    }

    public MatchInfo(GravSerializer serializer) {
        this.defendingName = serializer.readString();
        this.defendingBunker = serializer.readUUID();
        this.attackingName = serializer.readString();
        this.attackingBunker = serializer.readUUID();
        this.matchStats = new MatchStats(serializer);
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(defendingName);
        serializer.writeUUID(defendingBunker);
        serializer.writeString(attackingName);
        serializer.writeUUID(attackingBunker);
        matchStats.serialize(serializer);
    }
}
