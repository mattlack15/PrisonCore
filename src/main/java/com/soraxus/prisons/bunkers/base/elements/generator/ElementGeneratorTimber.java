package com.soraxus.prisons.bunkers.base.elements.generator;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

public class ElementGeneratorTimber extends GeneratorElement {
    public ElementGeneratorTimber(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker, BunkerResource.TIMBER);
    }

    public ElementGeneratorTimber(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    public int getDelay() {
        return 32;
    }

    @Override
    public double getAmount() {
        return 1;
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @NotNull
    @Override
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow("gen-timber-" + level + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public String getName() {
        return "Timber Generator";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.GENERATOR_TIMBER;
    }

    @Override
    public double getMaxHealth() {
        return this.getLevel() * 50 + 50;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

}
