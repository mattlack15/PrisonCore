package com.soraxus.prisons.bunkers.base.elements.defense.active.tower.laser;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.defense.active.ActiveDefenseElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;

public class ElementLaserTower extends ActiveDefenseElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementLaserTower(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return null;
    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public BunkerElementType getType() {
        return null;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
