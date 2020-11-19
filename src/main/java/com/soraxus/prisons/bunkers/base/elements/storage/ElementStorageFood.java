package com.soraxus.prisons.bunkers.base.elements.storage;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.DropResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;

public class ElementStorageFood extends StorageElement {
    public ElementStorageFood(Bunker bunker) {
        super(bunker, new Storage(BunkerResource.FOOD, 0, 125));
    }

    public ElementStorageFood(GravSerializer serializer, Bunker bunker) {
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
        return BunkerElementType.STORAGE_FOOD;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return new DropResource(BunkerResource.FOOD, (long) (this.getStorage(BunkerResource.FOOD).getAmount() / this.getMaxHealth() * damage));
    }
}
