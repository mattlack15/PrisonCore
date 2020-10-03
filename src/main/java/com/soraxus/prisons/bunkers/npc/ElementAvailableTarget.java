package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.BunkerElement;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

@AllArgsConstructor
public class ElementAvailableTarget implements AvailableTarget<BunkerElement> {
    private WeakReference<BunkerElement> e;

    public ElementAvailableTarget(BunkerElement e) {
        this(new WeakReference<>(e));
    }

    @Override
    public void apply(Targetter controller) {
        controller.setTarget(get());
    }

    @Override
    public @NotNull BunkerElement get() {
        BunkerElement el = e.get();
        if (el == null) {
            throw new IllegalStateException("No target found, please check exists first");
        }
        return el;
    }

    @Override
    public @NotNull Location getImmediateLocation() {
        return get().getBoundingRegion(1.8D * 2D).getCenter().toBukkitVector().toLocation(get().getBunker().getWorld().getBukkitWorld());
    }

    public boolean exists() {
        BunkerElement el = e.get();
        return el != null && !el.isDestroyed() && el.getBunker().getTileMap().byId(el.getId()) != null;
    }
}
