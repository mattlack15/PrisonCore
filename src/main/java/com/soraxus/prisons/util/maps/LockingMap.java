package com.soraxus.prisons.util.maps;

import com.soraxus.prisons.util.locks.CustomLock;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class LockingMap<K, V> extends HashMap<K, V> {
    @Getter
    private CustomLock lock = new CustomLock(true);

    public LockingMap(Map<? extends K, ? extends V> other) {
        super(other);
    }

    public LockingMap() {
        super();
    }

    public LockingMap<K, V> copy() {
        return lock.perform(() -> new LockingMap<>(this));
    }

    @Override
    public int size() {
        return lock.perform(super::size);
    }

    @Override
    public boolean isEmpty() {
        return lock.perform(super::isEmpty);
    }

    @Override
    public boolean containsKey(Object key) {
        return lock.perform(() -> super.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return lock.perform(() -> super.containsValue(value));
    }

    @Override
    public V get(Object key) {
        return lock.perform(() -> super.get(key));
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return lock.perform(() -> super.put(key, value));
    }

    @Override
    public V remove(Object key) {
        return lock.perform(() -> super.remove(key));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        lock.perform(() -> super.putAll(m));
    }

    @Override
    public void clear() {
        lock.perform(super::clear);
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return lock.perform(super::keySet);
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return lock.perform(super::values);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return lock.perform(super::entrySet);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> biConsumer) {
        lock.perform(() -> super.forEach(biConsumer));
    }
}
