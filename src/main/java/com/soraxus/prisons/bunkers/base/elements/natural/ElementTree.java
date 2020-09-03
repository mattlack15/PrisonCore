package com.soraxus.prisons.bunkers.base.elements.natural;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.NaturalElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.DropResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

public class ElementTree extends NaturalElement {
    /**
     * All non-abstract bunker child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching
     *
     * @param bunker     The Bunker this element is a part of
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     */
    public ElementTree(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
        setRotation(MathUtils.random(0, 3));
    }

    public ElementTree(Bunker bunker, int type, int size) {
        super(null, bunker);
        if (type < 1)
            type = 1;
        this.getMeta().set("tree-type", type);
        this.getMeta().set("tree-size", size);
        setRotation(MathUtils.random(0, 3));
        this.setHealth(getMaxHealth());
    }

    @Override
    public String getName() {
        return "Tree";
    }

    @Override
    public BunkerElementType getType() {
        return getTreeSize() == 1 ? BunkerElementType.NATURAL_TREE_SMALL_1 : BunkerElementType.NATURAL_TREE_MEDIUM_1;
    }

    public int getTreeType() {
        Object o = this.getMeta().get("tree-type");
        if(o == null)
            return 1;
        return (int) o;
    }

    public int getTreeSize() {
        Object o = this.getMeta().get("tree-size");
        if(o == null)
            return 1;
        return (int) o;
    }


    @Override
    public IntVector2D getShape() {
        return getTreeSize() == 1 ? new IntVector2D(1, 1) : (getTreeSize() == 2 ? new IntVector2D(2, 2) : new IntVector2D(3, 3));
    }

    @NotNull
    @Override
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.get("tree-" + (getTreeSize() == 1 ? "small" : (getTreeSize() == 2 ? "medium" : "large")) + "-" + getTreeType() + (destroyed ? "-destroyed" : ""));
    }

//    @Override
//    public BunkerElementType<? extends BunkerElement> getType() {
//        if (getTreeSize() == 1) {
//            return BunkerElementType.NATURAL.TREE_SMALL_1;
//        }
//        if (getTreeSize() == 2) {
//            return BunkerElementType.NATURAL.TREE_MEDIUM_1;
//        }
//        return null;
//    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public double getMaxHealth() {
        return 20 * getTreeSize();
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return new DropResource(BunkerResource.TIMBER, (damage));
    }

}
