package com.soraxus.prisons.bunkers.base.elements.research;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

public class ElementLaboratory extends BunkerElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementLaboratory(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementLaboratory(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(2, 2);
    }

    @Override
    public double getMaxHealth() {
        return 10;
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.RESEARCH_LABORATORY;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
