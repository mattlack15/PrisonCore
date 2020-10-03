package com.soraxus.prisons.gangs;

public class GangLevelUtil {
    public static long getReqXp(int level) {
        return 40L * level * level * level * level + 100;
    }
}
