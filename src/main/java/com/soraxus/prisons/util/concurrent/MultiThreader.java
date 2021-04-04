package com.soraxus.prisons.util.concurrent;

import lombok.AllArgsConstructor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class MultiThreader {
    private final ForkJoinPool service;

    /**
     * Create a MultiThreader with the default number of threads (available processor count)
     */
    public MultiThreader() {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Create a MultiThreader with a specific number of threads
     *
     * @param threads Thread count
     */
    public MultiThreader(int threads) {
        service = new ForkJoinPool(threads);
    }

    /**
     * Submit a task to this threader
     *
     * @param run Task
     */
    public void submit(Runnable run) {
        service.submit(run);
    }

    /**
     * Await termination with the default timeout (10 seconds)
     */
    public void awaitTermination() {
        awaitTermination(10000);
    }

    /**
     * Await termination with a custom timeout
     *
     * @param timeoutMillis Timeout in milliseconds
     */
    public void awaitTermination(int timeoutMillis) {
        try {
            service.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        this.service.shutdown();
    }

    public static MultiThreader common() {
        return new MultiThreader(ForkJoinPool.commonPool());
    }
}
