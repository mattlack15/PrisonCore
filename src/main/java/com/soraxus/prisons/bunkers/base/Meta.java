package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.util.MultiReentrantLock;
import com.soraxus.prisons.util.maps.LockingMap;
import lombok.NoArgsConstructor;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.ObjectDeserializationException;

/**
 * Holds all data for a bunker element
 * Is saved with the bunker
 * Thread-safe
 */
@NoArgsConstructor
@SuppressWarnings("unchecked")
public class Meta implements GravSerializable {
    private LockingMap<String, Object> meta = new LockingMap<>();
    private LockingMap<String, GravSerializer> toDeserialize = new LockingMap<>();

    public Meta(LockingMap<String, Object> meta) {
        this.meta = meta;
    }

    private MultiReentrantLock<String> lock = new MultiReentrantLock<>();

    /**
     * Load this element meta from a serializer
     *
     * @param serializer Serializer
     */
    public Meta(GravSerializer serializer) {
        if (serializer == null)
            return;
        int amount = serializer.readInt();
        for (int i = 0; i < amount; i++) {
            String key = serializer.readString();
            GravSerializer wrapper = serializer.readSerializer();
            try {
                wrapper.mark();
                Object object = wrapper.readObject();
                meta.put(key, object);
            } catch (ObjectDeserializationException e) {
                if (e.getDeserializationCause().equals(ObjectDeserializationException.DeserializationExceptionCause.NO_DESERIALIZATION_METHOD)) {
                    wrapper.reset();
                    toDeserialize.put(key, wrapper);
                }
            }
        }
    }

    public Meta getMeta(String key) {
        return new Meta(this.<LockingMap<String, Object>>getObject(key));
    }

    public long getLong(String key) {
        return get(key);
    }

    public void set(String key, Object object) {
        lock.lock(key);
        try {
            if (object instanceof Meta) {
                this.meta.put(key, ((Meta) object).meta);
            } else {
                this.meta.put(key, object);
            }
        } finally {
            lock.unlock(key);
        }
    }

    /**
     * Get an object from this element meta
     *
     * @param key              Key
     * @param constructionArgs if the object could not be constructed during deserialization, it will be attempt to be constructed again with these arguments
     * @return Object
     */
    public <T> T get(String key, Object... constructionArgs) {
        lock.lock(key);
        try {
            Object o = this.meta.get(key);
            if (o == null) {
                if (this.toDeserialize.containsKey(key)) {
                    GravSerializer serializer = toDeserialize.get(key);
                    o = serializer.readObject(constructionArgs); //Exception might be thrown here
                    Object finalO = o;
                    meta.getLock().perform(() -> {
                        toDeserialize.remove(key);
                        this.set(key, finalO);
                    }); //If nothing is thrown then remove and set
                    return (T) o;
                }
            }
            return (T) o;
        } finally {
            lock.unlock(key);
        }
    }

    public <T> T getOrSet(String key, T defaultValue, Object... constructionArgs) {
        lock.lock(key);
        try {
            Object object = get(key, constructionArgs);
            if (object == null) {
                set(key, defaultValue);
                return defaultValue;
            }
            return (T) object;
        } finally {
            lock.unlock(key);
        }
    }

    public <T> T getObject(String key, Object... arguments) {
        return get(key, arguments);
    }

    /**
     * Serialize this element meta into a serializer
     *
     * @param serializer Serializer
     */
    @Override
    public void serialize(GravSerializer serializer) {
        this.meta.getLock().perform(() -> {
            serializer.writeInt(meta.size() + toDeserialize.size());
            meta.forEach((k, o) -> {
                GravSerializer wrapper = new GravSerializer();
                wrapper.writeObject(o);
                serializer.writeString(k);
                serializer.writeSerializer(wrapper);
            });
            toDeserialize.forEach((k, o) -> {
                serializer.writeString(k);
                serializer.writeSerializer(o);
            });
        });
    }
}
