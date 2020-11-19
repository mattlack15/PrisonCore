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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class ElementStorageTimber extends StorageElement {
    public ElementStorageTimber(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementStorageTimber(Bunker bunker) {
        super(bunker, new Storage(BunkerResource.TIMBER, 0, 125));
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @NotNull
    @Override
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.get("storage-timber-" + level + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            e.setCancelled(true);
            new MenuStorageElement(this).open(e.getPlayer());
        }
    }

    @Override
    public String getName() {
        return "Timber Storage";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.STORAGE_TIMBER;
    }

    @Override
    public double getMaxHealth() {
        return this.getLevel() * 50 + 50;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return new DropResource(BunkerResource.TIMBER, (long) (this.getStorage(BunkerResource.TIMBER).getAmount() / this.getMaxHealth() * damage));
    }

    @Override
    public double getCapacity(BunkerResource resource) {
        return this.getLevel() * 100 + (this.getLevel() * this.getLevel() * 25);
    }

}
