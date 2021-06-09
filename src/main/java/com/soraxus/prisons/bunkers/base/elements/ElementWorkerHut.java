package com.soraxus.prisons.bunkers.base.elements;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.bunkers.workers.Worker;
import lombok.Getter;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

public class ElementWorkerHut extends BunkerElement {
    @Getter
    private Worker worker;

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementWorkerHut(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementWorkerHut(Bunker bunker) {
        this(null, bunker);
        this.worker = new Worker(this);
        getMeta().set("worker", worker);
    }

    @Override
    protected void onEnable() {
        worker = getMeta().get("worker", this);
    }

    @Override
    protected void onDisable() {
        getMeta().set("worker", worker);
    }

//    @Override
//    public BunkerElementType<? extends BunkerElement> getType() {
//        return BunkerElementType.ESSENTIAL.WORKER_HUT;
//    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @NotNull
    @Override
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow("worker-hut-" + Math.min(level, this.getMaxLevel()) + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public void onTick() {

    }

    @Override
    public double getMaxHealth() {
        return 50;
    }

    @Override
    public String getName() {
        return "Worker Hut";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ESSENTIAL_WORKER_HUT;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
