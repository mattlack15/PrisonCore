package com.soraxus.prisons.bunkers.base.elements.storage;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.DropResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

public class ElementStorageStone extends StorageElement {
    public ElementStorageStone(Bunker bunker) {
        super(bunker, new Storage(BunkerResource.STONE, 0, 125));
    }

    public ElementStorageStone(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }


    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow("storage-stone-" + level + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public void onTick() {

    }

    @Override
    public double getMaxHealth() {
        return this.getLevel() * 50 + 50;
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.STORAGE_STONE;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return new DropResource(BunkerResource.STONE, (long) (this.getStorage(BunkerResource.STONE).getAmount() / this.getMaxHealth() * damage));
    }

    @Override
    public double getCapacity(BunkerResource resource) {
        return this.getLevel() * 100 + (this.getLevel() * this.getLevel() * 25);
    }
}
