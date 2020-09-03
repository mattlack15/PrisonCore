package com.soraxus.prisons.bunkers.npc;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface AvailableTarget<T> {
    void apply(Targetter controller);
    @NotNull T get();
    @NotNull Location getImmediateLocation();
    boolean exists();
}
