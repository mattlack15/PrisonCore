package com.soraxus.prisons.util.list;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ElementableLockingList<T> extends LockingList<T> {
    /**
     * Get a value from this list that matches value
     *
     * @param value   Value
     * @param convert Function to convert from <T> to <B>
     * @param <B>     Type of value
     * @return Element for which convert returns value, {@code null} if none exists
     */
    public <B> T byFunction(B value, Function<T, B> convert) {
        return getLock().perform(() -> {
            for (T t : this) {
                if (convert.apply(t).equals(value)) {
                    return t;
                }
            }
            return null;
        });
    }

    public <B> T byFunctionOrAdd(B value, Function<T, B> convert, T el) {
        return getLock().perform(() -> {
            for (T t : this) {
                if (convert.apply(t).equals(value)) {
                    return t;
                }
            }
            add(el);
            return el;
        });
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