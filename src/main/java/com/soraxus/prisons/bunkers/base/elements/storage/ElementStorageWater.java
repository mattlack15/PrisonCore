package com.soraxus.prisons.bunkers.base.elements.storage;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.DropResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;

public class ElementStorageWater extends StorageElement {
    public ElementStorageWater(Bunker bunker) {
        super(bunker, new Storage(BunkerResource.WATER, 0, 125));
    }

    public ElementStorageWater(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    @Override
    public double getCapacity(BunkerResource resource) {
        return this.getLevel() * 100 + (this.getLevel() * this.getLevel() * 25);
    }
    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public double getMaxHealth() {
        return this.getLevel() * 50 + 50;
    }

    @Override
    public BunkerElementType getType() {
        return null;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return new DropResource(BunkerResource.WATER, (long) (this.getStorage(BunkerResource.WATER).getAmount() / this.getMaxHealth() * damage));
    }
}
