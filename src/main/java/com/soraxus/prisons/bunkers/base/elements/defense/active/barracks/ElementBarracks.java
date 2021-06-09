package com.soraxus.prisons.bunkers.base.elements.defense.active.barracks;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.defense.active.ActiveDefenseElement;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp.ElementArmyCamp;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Element for
 */
public class ElementBarracks extends ActiveDefenseElement {
    private NPC npc = null;

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
        return BunkerSchematics.getWithoutThrow("barracks-" + level + (destroyed ? "-destroyed" : ""));
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
            if (!camps.isEnabled())
                continue;
            if (camps.getNPCs().size() < camps.getCapacity()) {
                int toAdd = Math.min(camps.getCapacity() - camps.getNPCs().size(), amount);
                amount -= toAdd;
                for (int i = 0; i < toAdd; i++) {
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
    protected void onEnable() {
        EventSubscriptions.instance.subscribe(this);
        Synchronizer.synchronize(() -> {
            npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, ChatColor.YELLOW + "Sergeant " + ChatColor.WHITE + "Umer");
            npc.spawn(getBoundingRegion(1).getCenter().toBukkitVector().toLocation(getBunker().getWorld().getBukkitWorld()));
        });
    }

    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribe(this);
        Synchronizer.synchronize(() -> {
            npc.despawn();
            npc.destroy();
        });
    }

    public void makeNPCSayNear(String message) {
        makeNPCSayNear(message, 0);
    }

    public void makeNPCSayNear(String message, double distance) {
        if (getBunker().getWorld() == null) {
            return;
        }
        List<Player> players = getBunker().getWorld().getBukkitWorld().getPlayers();
        CuboidRegion region = getBoundingRegion(20);
        ChatBuilder chat = ChatBuilder.prefix("&e&lSergeant &fUmer &l> &7")
                .addText(message);
        players.stream()
                .filter(player -> region.smallestDistance(Vector3D.fromBukkitVector(player.getLocation().toVector())) <= distance)
                .collect(Collectors.toList())
                .forEach(chat::send);
    }

    public void makeNPCSayGang(String message) {
        ChatBuilder chat = ChatBuilder.prefix("&e&lSergeant &fUmer &l> &7")
                .addText(message);
        getBunker().getGang().getMembers().stream()
                .map((m) -> Bukkit.getPlayer(m.getMember()))
                .filter(Objects::nonNull)
                .forEach(chat::send);
    }

    @Override
    protected void onPlacement() {
        makeNPCSayNear("Sergeant Umer ready for duty sir!", 28);
    }

    @EventSubscription
    private void onNPCClick(NPCRightClickEvent event) {
        if (event.getNPC() != npc || npc == null)
            return;
        new MenuBarracks(this).open(event.getClicker());
    }
}