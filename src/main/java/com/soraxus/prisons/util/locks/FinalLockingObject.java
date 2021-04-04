package com.soraxus.prisons.util.locks;

import java.util.function.Consumer;
import java.util.function.Function;

public class FinalLockingObject<T> {
    private final T object;
    private CustomLock lock;
    
    public FinalLockingObject() {
        this(null);
    }
    
    public FinalLockingObject(T obj) {
        this.object = obj;
    }
    
    public void perform(Consumer<T> func) {
        lock.perform(() -> func.accept(object));
    }
    public <O> O perform(Function<T, O> func) {
        return lock.perform(() -> func.apply(object));
    }
}
