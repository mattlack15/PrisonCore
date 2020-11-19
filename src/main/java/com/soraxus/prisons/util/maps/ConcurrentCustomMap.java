package com.soraxus.prisons.util.maps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentCustomMap<K, V> extends ConcurrentHashMap<K, V> {
    public ConcurrentCustomMap() {
        super();
    }

    public ConcurrentCustomMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public V getOrSet(K key, V def) {
        if (!containsKey(key)) {
            put(key, def);
        }
        return get(key);
    }
}
