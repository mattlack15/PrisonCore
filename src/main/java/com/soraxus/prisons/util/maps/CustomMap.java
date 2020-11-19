package com.soraxus.prisons.util.maps;

import java.util.HashMap;

public class CustomMap<K, V> extends HashMap<K, V> {
    public CustomMap() {
        super();
    }

    public V getOrSet(K key, V def) {
        if (!containsKey(key)) {
            put(key, def);
        }
        return get(key);
    }
}
