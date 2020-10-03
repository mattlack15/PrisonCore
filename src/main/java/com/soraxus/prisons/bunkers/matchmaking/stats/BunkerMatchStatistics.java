package com.soraxus.prisons.bunkers.matchmaking.stats;

import com.soraxus.prisons.bunkers.matchmaking.MatchInfo;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BunkerMatchStatistics implements GravSerializable {
    private double winRate = 0.0;
    private int totalMatches = 0;
    private int wins = 0;
    private int losses = 0;
    private List<MatchInfo> previousMatches;

    public BunkerMatchStatistics() {
        this.previousMatches = new ArrayList<>();
    }

    public BunkerMatchStatistics(List<MatchInfo> previousMatches) {
        this.previousMatches = previousMatches;
        count();
        recalculate();
    }

    public BunkerMatchStatistics(GravSerializer serializer) {
        this(serializer.<List<MatchInfo>>readObject());
    }

    public void count() {
        int wins = 0;
        for (MatchInfo info : previousMatches) {
            if (info.getMatchStats().getAttackerSuccess()) {
                wins ++;
            }
        }
        this.wins = wins;
    }

    public void recalculate() {
        if (previousMatches.isEmpty()) {
            return; // No dividing by 0 here!
        }
        this.totalMatches = previousMatches.size();
        this.losses = this.totalMatches - this.wins;
        this.winRate = (double) wins / totalMatches;
    }

    public void setMatches(List<MatchInfo> newInfos) {
        this.previousMatches = newInfos;
        count();
        recalculate();
    }

    public void addMatch(MatchInfo newInfo) {
        this.totalMatches ++;
        if (newInfo.getMatchStats().getAttackerSuccess()) {
            this.wins ++;
        } else {
            this.losses ++;
        }
        recalculate();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(this.previousMatches);
    }
}
