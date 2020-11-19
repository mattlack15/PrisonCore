package com.soraxus.prisons.util;

import java.util.concurrent.atomic.AtomicReference;

public class WaiterObject<T> {
    private AtomicReference<T> reference;

    public WaiterObject(T value) {
        this.reference = new AtomicReference<>(value);
    }
}
