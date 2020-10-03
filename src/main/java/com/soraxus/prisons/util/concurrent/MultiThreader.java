package com.soraxus.prisons.util.concurrent;

import java.util.concurrent.ForkJoinPool;

public class MultiThreader {
    private final ForkJoinPool service;
    public MultiThreader(int threads) {
        service = new ForkJoinPool(threads);
    }
}
