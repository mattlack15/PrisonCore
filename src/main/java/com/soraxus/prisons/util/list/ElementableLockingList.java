package com.soraxus.prisons.util.list;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ElementableLockingList<T> extends LockingList<T> {
    public <B> T byFunction(B value, Function<T, B> convert) {
        getLock().lock();
        try {
            for (T t : this) {
                if (convert.apply(t).equals(value)) {
                    return t;
                }
            }
            return null;
        } finally {
            getLock().unlock();
        }
    }

    public <B> void modifyIfFunctionEqual(T value, Function<T, B> convert, BiConsumer<T, T> applier) {
        getLock().perform(() -> {
            T t = byFunction(convert.apply(value), convert);
            if (t == null) {
                add(value);
            } else {
                applier.accept(t, value);
            }
        });
    }
}