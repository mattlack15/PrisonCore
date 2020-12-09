package com.soraxus.prisons.sorting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SortingTask<T> {
    private final Supplier<Map<T, Long>> supplier;
    @Getter
    private final boolean requireSync;

    private Map<T, Long> cached;
    @Getter
    private Map<T, Long> sorted;

    /**
     * If <code>requiredSync</code> is true, call synchronously
     */
    void cache() {
        this.cached = supplier.get();
    }

    /**
     * Should be called asynchronously
     */
    void sort() {
        sorted = cached.entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
}
