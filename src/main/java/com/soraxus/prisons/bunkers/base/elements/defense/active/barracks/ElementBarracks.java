package com.soraxus.prisons.bunkers.base.elements.defense.active.barracks;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp.ElementArmyCamp;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Element for
 */
public class ElementBarracks extends BunkerElement {

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementBarracks(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementBarracks(Bunker bunker) {
        this(null, bunker);
    }

    public List<BunkerNPCType> getAvailableTypes() {
        List<BunkerNPCType> typeList = new ArrayList<>();
        for (BunkerNPCType type : BunkerNPCType.values()) {
            if (type.getRequiredBarracksLevel() <= this.getLevel())
                typeList.add(type);
        }
        return typeList;
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(2, 2);
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.get("barracks-" + level + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public void onTick() {
        List<BunkerNPCType> typeList = new ArrayList<>();
        this.getTrainingList().removeIf(t -> {
            if (typeList.contains(t.getType()))
                return false;
            typeList.add(t.getType());
            if (t.decrementTicks()) {
                generate(t.getType(), 1, t.getLevel());
                return true;
            }
            return false;
        });
    }

    @Override
    public double getMaxHealth() {
        return getLevel() * 125;
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ARMY_BARRACKS;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    public void generate(BunkerNPCType type, int amount, int level) {
        //Create
        List<BunkerNPC> npcList = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            BunkerNPC npc = new BunkerNPC(type, level);
            npcList.add(npc);
        }

        //Distribute
        Iterator<BunkerNPC> it = npcList.iterator();
        for (ElementArmyCamp camps : getBunker().getTileMap().byClass(ElementArmyCamp.class)) {
            if (amount <= 0 || !it.hasNext())
                break;
            if (camps.getNPCs().size() < camps.getCapacity()) {
                int toAdd = Math.min(camps.getCapacity() - camps.getNPCs().size(), amount);
                amount -= toAdd;
                for (int i = 0; i < toAdd; i++) {
                    Bukkit.broadcastMessage("Adding next npc to camp");
                    camps.addNPC(it.next());
                    it.remove();
                }
            }
        }
    }

    public void startGeneration(BunkerNPCType type, int level) {
        getTrainingList().add(new ProcessNPCTraining(type, type.getGenerationTime(level), level));
    }

    public List<ProcessNPCTraining> getTrainingList() {
        return getMeta().getOrSet("training", Collections.synchronizedList(new ArrayList<>()));
    }

    public List<ProcessNPCTraining> getTrainingListByType(BunkerNPCType type) {
        List<ProcessNPCTraining> trainingList = new ArrayList<>(getTrainingList());
        trainingList.removeIf(t -> !t.getType().equals(type));
        return trainingList;
    }

    @Override
    public void onClick(PlayerInteractEvent e) {
        new MenuBarracks(this).open(e.getPlayer());
    }
}