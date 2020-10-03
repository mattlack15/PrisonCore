package com.soraxus.prisons.bunkers.base;

import net.ultragrav.serializer.GravSerializer;

public abstract class NaturalElement extends BunkerElement {
    /**
     * All non-abstract bunker child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public NaturalElement(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    @Override
    public void onTick() {}

    @Override
    public boolean onDestroy() {
        this.remove();
        return false;
    }

    @Override
    public boolean isVisibleToAttackers() {
        return false;
    }
}
