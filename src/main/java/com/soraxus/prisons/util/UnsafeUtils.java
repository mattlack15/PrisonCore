package com.soraxus.prisons.util;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class UnsafeUtils {
    private static Unsafe unsafe = getUnsafe();

    @NotNull
    static Unsafe getUnsafe() {
        if (unsafe != null)
            return unsafe;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (unsafe = (Unsafe) f.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int NR_BITS = Integer.parseInt(System.getProperty("sun.arch.data.model"));
    private static final int BYTE = 8;
    private static final int WORD = NR_BITS / BYTE;
    private static final int MIN_SIZE = 16;

    public static int sizeOf(Class<?> src) {
        List<Field> instanceFields = new LinkedList<>();
        do {
            if (src == Object.class) return MIN_SIZE;
            for (Field f : src.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    instanceFields.add(f);
                }
            }
            src = src.getSuperclass();
        } while (instanceFields.isEmpty());
        long maxOffset = 0;
        for (Field f : instanceFields) {
            long offset = getUnsafe().objectFieldOffset(f);
            if (offset > maxOffset) maxOffset = offset;
        }
        return (((int) maxOffset / WORD) + 1) * WORD;
    }

    public static <T> T shallowClone(T object) throws InstantiationException {
        Class<?> src = object.getClass();

        int size;
        List<Field> instanceFields = new LinkedList<>();
        do {
            for (Field f : src.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    instanceFields.add(f);
                }
            }
            src = src.getSuperclass();
        } while (instanceFields.isEmpty());
        if (object.getClass() == Object.class) {
            size = MIN_SIZE;
        } else {
            long maxOffset = 0;
            for (Field f : instanceFields) {
                long offset = getUnsafe().objectFieldOffset(f);
                if (offset > maxOffset) maxOffset = offset;
            }
            size = (((int) maxOffset / WORD) + 1) * WORD;
        }
        List<Integer> referenceOffsets = new ArrayList<>();
        instanceFields.forEach(f -> {
            if(!f.getType().isPrimitive()) {
                referenceOffsets.add((int) getUnsafe().objectFieldOffset(f));
            }
        });
        referenceOffsets.sort(Integer::compareTo);

        @SuppressWarnings("unchecked")
        T newObject = (T) getUnsafe().allocateInstance(object.getClass());

        Iterator<Integer> refIterator = referenceOffsets.iterator();

        int nextReference = refIterator.hasNext() ? refIterator.next() : size;
        int referenceSize = Unsafe.ARRAY_OBJECT_INDEX_SCALE;
        int passed = Unsafe.ADDRESS_SIZE + referenceSize; //Skip object header (mark and pointer)
        while(passed < size) {
            while(nextReference - passed >> 3 != 0) {
                getUnsafe().putLong(newObject, passed, getUnsafe().getLong(object, passed));
                passed += 8;
            }
            while(nextReference - passed >> 2 != 0) {
                getUnsafe().putInt(newObject, passed, getUnsafe().getInt(object, passed));
                passed += 4;
            }
            while(nextReference - passed != 0) {
                getUnsafe().putByte(newObject, passed, getUnsafe().getByte(object, passed));
                passed++;
            }
            if(passed >= size)
                break;
            getUnsafe().putObject(newObject, passed, getUnsafe().getObject(object, passed));
            nextReference = refIterator.hasNext() ? refIterator.next() : size;
            passed += referenceSize;
        }
        return newObject;
    }

    /**
     * This doesn't work yet, someone has to code it
     *
     * @param object
     * @param <T>
     * @return
     * @throws InstantiationException
     */
    public static <T> T deepClone(T object, Map<Object, Object> referenceMap) throws InstantiationException {
        Class<?> src = object.getClass();

        int size;
        List<Field> instanceFields = new LinkedList<>();
        do {
            for (Field f : src.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    instanceFields.add(f);
                }
            }
            src = src.getSuperclass();
        } while (instanceFields.isEmpty());

        if (object.getClass() == Object.class) {
            size = MIN_SIZE;
        } else {
            long maxOffset = 0;
            for (Field f : instanceFields) {
                long offset = getUnsafe().objectFieldOffset(f);
                if (offset > maxOffset) maxOffset = offset;
            }
            size = (((int) maxOffset / WORD) + 1) * WORD;
        }
        List<Integer> referenceOffsets = new ArrayList<>();
        instanceFields.forEach(f -> {
            if(!f.getType().isPrimitive()) {
                referenceOffsets.add((int) getUnsafe().objectFieldOffset(f));
            }
        });
        referenceOffsets.sort(Integer::compareTo);

        @SuppressWarnings("unchecked")
        T newObject = (T) getUnsafe().allocateInstance(object.getClass());

        Map<Object, Object> newReferences = new HashMap<>(referenceMap);
        newReferences.put(object, newObject);

        Iterator<Integer> refIterator = referenceOffsets.iterator();

        int nextReference = refIterator.hasNext() ? refIterator.next() : size;
        int referenceSize = Unsafe.ARRAY_OBJECT_INDEX_SCALE;
        int passed = Unsafe.ADDRESS_SIZE + referenceSize; //Skip object header (mark and pointer)
        while(passed < size) {
            while(nextReference - passed >> 3 != 0) {
                getUnsafe().putLong(newObject, passed, getUnsafe().getLong(object, passed));
                passed += 8;
            }
            while(nextReference - passed >> 2 != 0) {
                getUnsafe().putInt(newObject, passed, getUnsafe().getInt(object, passed));
                passed += 4;
            }
            while(nextReference - passed != 0) {
                getUnsafe().putByte(newObject, passed, getUnsafe().getByte(object, passed));
                passed++;
            }
            if(passed >= size)
                break;

            //After this, the next thing to copy is an object

            // Get the object from the reference.
            Object obj = getUnsafe().getObject(object, passed);
            if (newReferences.containsKey(obj)) {
                obj = newReferences.get(obj);
            } else {
                obj = deepClone(obj, newReferences);
            }

            getUnsafe().putObject(newObject, passed, obj);
            nextReference = refIterator.hasNext() ? refIterator.next() : size;
            passed += referenceSize;
        }
        return newObject;
    }
}
