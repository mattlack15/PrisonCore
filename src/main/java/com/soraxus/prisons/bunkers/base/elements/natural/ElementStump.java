package com.soraxus.prisons.bunkers.base.elements.natural;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.NaturalElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

public class ElementStump extends NaturalElement {
    /**
     * All non-abstract bunker child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching
     *
     * @param bunker     The Bunker this element is a part of
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     */
    public ElementStump(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
        setRotation(MathUtils.random(0, 3));
    }

    public ElementStump(Bunker bunker, int type) {
        super(null, bunker);
        if (type < 1)
            type = 1;
        this.getMeta().set("stump-type", type);
        setRotation(MathUtils.random(0, 3));
    }

    @Override
    public String getName() {
        return "Stump";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.NATURAL_STUMP_1;
    }

    public int getStumpType() {
        return this.getMeta().get("stump-type");
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(1, 1);
    }

    @NotNull
    @Override
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow("stump-" + getStumpType() + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public double getMaxHealth() {
        return 50D;
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
