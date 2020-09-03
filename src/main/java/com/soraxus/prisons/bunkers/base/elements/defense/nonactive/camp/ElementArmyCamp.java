package com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElementArmyCamp extends BunkerElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementArmyCamp(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
        getNPCs();
    }

    public ElementArmyCamp(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(2, 2);
    }

    public synchronized List<BunkerNPC> getNPCs() {
        List<BunkerNPC> npcList = getMeta().getObject("npcs");
        if(npcList == null) {
            npcList = Collections.synchronizedList(new ArrayList<>());
            setNPCs(npcList);
        }
        return npcList;
    }

    public synchronized void setNPCs(List<BunkerNPC> stored) {
        getMeta().set("npcs", stored);
    }

    public synchronized void addNPC(BunkerNPC npc) {
        getNPCs().add(npc);
    }

    public synchronized void clearNPCs() {
        setNPCs(new ArrayList<>());
    }

    public synchronized void removeNPC(BunkerNPCType type) {
        getNPCs().removeIf(n -> n.getType().equals(type));
    }

    public int getCapacity() {
        return this.getLevel() * 25;
    }

    @Override
    public void onTick() {

    }

    @Override
    public double getMaxHealth() {
        return getLevel() * 100;
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ARMY_CAMP;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
