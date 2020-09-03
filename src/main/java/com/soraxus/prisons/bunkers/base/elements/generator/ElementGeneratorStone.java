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

public class ElementGeneratorStone extends GeneratorElement {

    public ElementGeneratorStone(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker, BunkerResource.STONE);
    }

    public ElementGeneratorStone(Bunker bunker) {
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
        return BunkerSchematics.get("gen-stone-" + level + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.GENERATOR_STONE;
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
