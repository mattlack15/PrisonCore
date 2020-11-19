package com.soraxus.prisons.bunkers.base.elements.natural;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.NaturalElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;

public class ElementPond extends NaturalElement {
    /**
     * All non-abstract bunker child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching
     *
     * @param bunker     The Bunker this element is a part of
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     */
    public ElementPond(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
        setRotation(MathUtils.random(0, 3));
    }

    public ElementPond(Bunker bunker, int type) {
        super(null, bunker);
        if (type < 1)
            type = 1;
        this.getMeta().set("pond-type", type);
        setRotation(MathUtils.random(0, 3));
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.NATURAL_POND_1;
    }

    public int getPondType() {
        return this.getMeta().get("pond-type");
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(1, 1);
    }

    @Override
    public double getMaxHealth() {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
