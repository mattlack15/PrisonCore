package com.soraxus.prisons.bunkers;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class BunkerDebugStats {
    public enum DebugStat {
        BUNKER_LOAD_SYNC,
        BUNKER_LOAD_ASYNC,
        BUNKER_LOAD_TOTAL;
    }

    //TODO

    @Getter
    Map<DebugStat, AvgMeasure> measureMap = new HashMap<>();
}
