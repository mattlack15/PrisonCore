/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.list.LockingList;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronizer {

    private static class QueuedRunnable {
        public final Runnable runnable;
        public final AtomicBoolean executing = new AtomicBoolean(false);

        public QueuedRunnable(Runnable runnable) {
            this.runnable = runnable;
        }
    }

    private static final LockingList<QueuedRunnable> workQueue = new LockingList<>();
    private static boolean closed = false;

    public static int synchronize(Runnable run) {
        if (Bukkit.isPrimaryThread()) {
            run.run();
            return -1;
        } else {
            return workQueue.getLock().perform(() -> {
                if (closed)
                    throw new RuntimeException("Synchronizer closed!");
                QueuedRunnable q = new QueuedRunnable(run);
                workQueue.add(q);
                return Bukkit.getScheduler().runTask(SpigotPrisonCore.instance, () -> {
                    if (q.executing.compareAndSet(false, true)) {
                        workQueue.remove(q);
                        q.runnable.run();
                    }
                }).getTaskId();
            });
        }
    }

    public static void closeAndFinish() {
        if (!Bukkit.isPrimaryThread()) {
            throw new RuntimeException("Bruh, this can't be called outside of the main thread...");
        }
        workQueue.getLock().perform(() -> {
            closed = true;
            for (QueuedRunnable q : workQueue) {
                if (q.executing.compareAndSet(false, true)) {
                    workQueue.remove(q);
                    q.runnable.run();
                }
            }
        });
    }

    /**
     * Finish off all waiting tasks
     */
    public static void finishRunnables() {
        finishRunnables(-1);
    }

    /**
     * Finish off an amount of waiting tasks
     */
    public static void finishRunnables(int maxAmount) {
        if (!Bukkit.isPrimaryThread()) {
            throw new RuntimeException("Bruh, this can't be called outside of the main thread...");
        }
        workQueue.getLock().perform(() -> {
            int i = 0;
            for (QueuedRunnable q : workQueue) {
                if (q.executing.compareAndSet(false, true)) {
                    workQueue.remove(q);
                    q.runnable.run();
                }
                if (++i >= maxAmount)
                    break;
            }
        });
    }

    public static int desynchronize(Runnable run) {
        return Bukkit.getScheduler().runTaskAsynchronously(SpigotPrisonCore.instance, run).getTaskId();
    }
}
