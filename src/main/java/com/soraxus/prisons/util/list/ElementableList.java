package com.soraxus.prisons.util.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ElementableList<T> extends ArrayList<T> {
    public ElementableList() {
        super();
    }

    public ElementableList(List<T> other) {
        super(other);
    }

    /**
     * Get a value from this list that matches value
     *
     * @param value   Value
     * @param convert Function to convert from <T> to <B>
     * @param <B>     Type of value
     * @return Element for which convert returns value, {@code null} if none exists
     */
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

    /**
     * Add a value to this list if it is not already present
     *
     * @param value Value
     * @return value
     */
    public T addIfNotContains(T value) {
        if (contains(value)) {
            return null;
        }
        add(value);
        return value;
    }

    /**
     * Count the number of elements that satisfy a condition
     *
     * @param compare Condition
     * @return Number of elements
     */
    public int count(Function<T, Boolean> compare) {
        int count = 0;
        for (T t : this) {
            if (compare.apply(t)) {
                count++;
            }
        }
        return count;
    }

    public <K> void addAllLists(Collection<K> lists, Function<K, Collection<T>> converter) {
        for (K k : lists) {
            addAll(converter.apply(k));
        }
    }

    public long sum(Function<T, Long> converter) {
        long sum = 0;
        for (T t : this) {
            sum += converter.apply(t);
        }
        return sum;
    }
}