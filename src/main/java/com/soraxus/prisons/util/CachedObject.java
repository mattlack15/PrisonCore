/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util;

import java.util.function.Supplier;

public class CachedObject<T> {
    private final long timeOut;
    private long lastUpdate;
    private T cached;
    private final Supplier<T> supplier;

    public CachedObject(Supplier<T> supplier, long timeOut) {
        this.supplier = supplier;
        this.timeOut = timeOut;
        recache();
    }

    public void recache() {
        this.cached = supplier.get();
        lastUpdate = System.currentTimeMillis();
    }

    public T get() {
        if (System.currentTimeMillis() - lastUpdate > timeOut) {
            recache();
        }
        return this.cached;
    }
}
