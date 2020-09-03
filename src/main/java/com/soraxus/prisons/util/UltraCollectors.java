/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UltraCollectors {
    /**
     * Create a LinkedHashMap from a stream of entries
     * Note: Only use when an ordered map is necessary
     * Otherwise it is wiser to use toMap
     *
     * All arguments are inferred from the stream
     * @param <K> Key
     * @param <V> Value
     * @return LinkedHashMap<Key, Value>
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toLinkedHashMap() {
        return new Collector<Map.Entry<K, V>, LinkedHashMap<K, V>, LinkedHashMap<K, V>>() {
            @Override
            public Supplier<LinkedHashMap<K, V>> supplier() {
                return LinkedHashMap::new;
            }

            @Override
            public BiConsumer<LinkedHashMap<K, V>, Map.Entry<K, V>> accumulator() {
                return (map, entry) -> map.put(entry.getKey(), entry.getValue());
            }

            @Override
            public BinaryOperator<LinkedHashMap<K, V>> combiner() {
                return (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                };
            }

            @Override
            public Function<LinkedHashMap<K, V>, LinkedHashMap<K, V>> finisher() {
                return map -> map;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    /**
     * Create a Map from a stream of entries
     * @param <K> Key
     * @param <V> Value
     * @return Map<Key, Value>
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
