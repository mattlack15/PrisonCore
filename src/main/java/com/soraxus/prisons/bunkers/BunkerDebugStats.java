package com.soraxus.prisons.bunkers;

import java.util.HashMap;
import java.util.Map;

public class BunkerDebugStats {
    public enum DebugStat {
        BUNKER_LOAD_SYNC,
        BUNKER_LOAD_ASYNC,
        BUNKER_LOAD_TOTAL;
    }

    public static Map<DebugStat, AvgMeasure> measureMap = new HashMap<>();
    static {
        measureMap.put(DebugStat.BUNKER_LOAD_TOTAL, new AvgMeasure());
    }
}
