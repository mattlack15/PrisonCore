package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallParameter;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallRotation;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Standardised class for elements taht connect to each other
 */
public abstract class ConnectingElement extends BunkerElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ConnectingElement(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    protected List<Class<? extends BunkerElement>> getConnectables() {
        return Collections.singletonList(getClass());
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        BunkerElement[] neighbours = getNeighbours();
        WallParameter param = WallRotation.get(neighbours, getConnectables());
        this.setRotation(param.getRotation());
        return BunkerSchematics.get(getName().replaceAll(" ", "-").toLowerCase() + "-" + getLevel() + "-" + param.getType().name().toLowerCase() + (destroyed ? "-destroyed" : ""));
    }
}