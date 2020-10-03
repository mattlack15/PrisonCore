package com.soraxus.prisons.bunkers.base.elements.decoration;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementGate;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementWall;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallParameter;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallType;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ElementPath extends BunkerElement {

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementPath(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementPath(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public void onUpdate() {
        this.build(false);
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        BunkerElement[] neighbours = getNeighbours();
        int[] bls = Arrays.stream(neighbours)
                .mapToInt(n -> {
                    if(n == null || n instanceof ElementWall && !(n instanceof ElementGate))
                        return 0;
                    return 1;
                }).toArray();
        WallParameter param = WallType.getWall(bls);
        this.setRotation(param.getRotation());
        return BunkerSchematics.get("path-" + param.getType().name().toLowerCase() + "-" + getLevel());
    }

    @Override
    public boolean isVisibleToAttackers() {
        return false;
    }

    @Override
    public double getMaxHealth() {
        return 10;
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.DECORATION_PATH;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
