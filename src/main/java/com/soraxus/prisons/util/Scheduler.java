/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util;

import com.soraxus.prisons.SpigotPrisonCore;
import org.bukkit.Bukkit;

public class Scheduler {
    /**
     * Runs the runnable after delay seconds
     *
     * @param run   - The runnable to run
     * @param delay - The delay in seconds
     * @return The Task ID for the task created
     */
    public static int scheduleSyncDelayedTask(Runnable run, int delay) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(SpigotPrisonCore.instance, run, delay * 20);
    }

    /**
     * Runs the runnable after delay seconds
     *
     * @param run    - The runnable to run
     * @param delay  - The delay in seconds
     * @param repeat - The delay between repeats in seconds
     * @return The Task ID for the task created
     */
    public static int scheduleSyncRepeatingTask(Runnable run, int delay, int repeat) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(SpigotPrisonCore.instance, run, delay * 20, repeat * 20);
    }

    /**
     * Runs the runnable after delay ticks
     *
     * @param run   - The runnable to run
     * @param delay - The delay in ticks
     * @return The Task ID for the task created
     */
    public static int scheduleSyncDelayedTaskT(Runnable run, int delay) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(SpigotPrisonCore.instance, run, delay);
    }

    /**
     * Runs the runnable after delay ticks
     *
     * @param run    - The runnable to run
     * @param delay  - The delay in seconds
     * @param repeat - The delay between repeats in ticks
     * @return The Task ID for the task created
     */
    public static int scheduleSyncRepeatingTaskT(Runnable run, int delay, int repeat) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(SpigotPrisonCore.instance, run, delay, repeat);
    }
}