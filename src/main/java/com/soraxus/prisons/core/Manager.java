package com.soraxus.prisons.core;

import java.util.List;
import java.util.concurrent.Future;

public abstract class Manager<H, I> {
    public abstract List<H> getLoaded();

    public abstract boolean add(H object);

    public abstract H get(I identifier);

    public abstract Future<H> load(I identifier);

    public abstract Future<Void> unload(I identifier);

    public abstract Future<Void> remove(I identifier);
}