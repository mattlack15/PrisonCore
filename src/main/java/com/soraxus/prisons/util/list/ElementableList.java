package com.soraxus.prisons.util.list;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ElementableList<T> extends ArrayList<T> {
    public <B> T byFunction(B value, Function<T, B> convert) {
        for (T t : this) {
            if (convert.apply(t).equals(value)) {
                return t;
            }
        }
        return null;
    }

    public <B> void modifyIfFunctionEqual(T value, Function<T, B> convert, BiConsumer<T, T> applier) {
        T t = byFunction(convert.apply(value), convert);
        if (t == null) {
            add(value);
        } else {
            applier.accept(t, value);
        }
    }

    public T addIfNotContains(T value) {
        if (contains(value)) {
            return null;
        }
        add(value);
        return value;
    }
}