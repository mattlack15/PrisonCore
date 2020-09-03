/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util;

import com.soraxus.prisons.SpigotPrisonCore;
import org.bukkit.Bukkit;

public class Synchronizer {
    public static int synchronize(Runnable run) {
        if (Bukkit.isPrimaryThread()) {
            run.run();
            return -1;
        } else {
            return Bukkit.getScheduler().runTask(SpigotPrisonCore.instance, run).getTaskId();
        }
    }

    public static int desynchronize(Runnable run) {
        return Bukkit.getScheduler().runTaskAsynchronously(SpigotPrisonCore.instance, run).getTaskId();
    }
}
