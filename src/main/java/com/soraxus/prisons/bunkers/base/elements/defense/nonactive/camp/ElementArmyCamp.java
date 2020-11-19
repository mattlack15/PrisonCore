package com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ElementArmyCamp extends BunkerElement {
    private static final Vector3D CAMP_LEADER_POS = new Vector3D(3.5, 0, 8.5);
    private NPC campLeader;

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

    private synchronized List<BunkerNPC> getNPCs0() {
        List<BunkerNPC> npcList = getMeta().getObject("npcs");
        if (npcList == null) {
            npcList = Collections.synchronizedList(new ArrayList<>());
            setNPCs(npcList);
        }
        return npcList;
    }

    public synchronized List<BunkerNPC> getNPCs() {
        return new ArrayList<>(getNPCs0());
    }

    public synchronized void setNPCs(List<BunkerNPC> stored) {
        getMeta().set("npcs", stored);
    }

    public synchronized void addNPC(BunkerNPC npc) {
        getNPCs0().add(npc);
    }

    public synchronized void clearNPCs() {
        setNPCs(new ArrayList<>());
    }

    public synchronized void clearNPC(BunkerNPCType type) {
        getNPCs0().removeIf(n -> n.getType().equals(type));
    }

    public synchronized int count(BunkerNPCType type) {
        int counter = 0;
        for (BunkerNPC npc : getNPCs0()) {
            if (npc.getType().equals(type))
                counter++;
        }
        return counter;
    }

    public synchronized boolean removeNPCs(BunkerNPCType type, int amount) {
        if (count(type) < amount)
            return false;
        AtomicInteger toRemove = new AtomicInteger(amount);
        getNPCs0().removeIf(n -> toRemove.decrementAndGet() >= 0);
        return true;
    }

    public synchronized BunkerNPC removeOne(BunkerNPCType type) {
        for (BunkerNPC npc : getNPCs()) {
            if (npc.getType().equals(type)) {
                getNPCs0().remove(npc);
                return npc;
            }
        }
        return null;
    }

    public int getCapacity() {
        return this.getLevel() * 25;
    }

    @Override
    public void onTick() {
        if (getBunker().getResourceAmount(BunkerResource.FOOD) == 0 || getBunker().getResourceAmount(BunkerResource.WATER) == 0) {
            if (MathUtils.isRandom(1, 50)) {
                synchronized (this) {
                    if (getNPCs0().size() != 0)
                        getNPCs0().remove(MathUtils.random(0, getNPCs0().size() - 1));
                }
            }
        }
    }

    @Override
    public double getMaxHealth() {
        return getLevel() * 100;
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ARMY_CAMP;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    @Override
    protected void onEnable() {
        EventSubscriptions.instance.subscribe(this);
        campLeader = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, ChatColor.BLUE + "Camp Leader");
        campLeader.spawn(CAMP_LEADER_POS.toBukkitVector().toLocation(getBunker().getWorld().getBukkitWorld()).add(this.getLocation()));
    }

    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribe(this);
        if (campLeader.isSpawned()) {
            campLeader.despawn();
            campLeader.destroy();
        }
    }

    public void makeCampLeaderSayNear(String message) {
        makeCampLeaderSayNear(message, 0);
    }

    public void makeCampLeaderSayNear(String message, double distance) {
        if (getBunker().getWorld() == null) {
            return;
        }
        List<Player> players = getBunker().getWorld().getBukkitWorld().getPlayers();
        CuboidRegion region = getBoundingRegion(20);
        ChatBuilder chat = ChatBuilder.prefix("&bCamp Leader #s> ")
                .addText(message);
        players.stream()
                .filter(player -> region.smallestDistance(Vector3D.fromBukkitVector(player.getLocation().toVector())) <= distance)
                .collect(Collectors.toList())
                .forEach(chat::send);
    }

    public void makeCampLeaderSayGang(String message) {
        ChatBuilder chat = ChatBuilder.prefix("&bCamp Leader #s> ")
                .addText(message);
        getBunker().getGang().getMembers().stream()
                .map((m) -> Bukkit.getPlayer(m.getMember()))
                .filter(Objects::nonNull)
                .forEach(chat::send);
    }

    @EventSubscription
    private void onLeaderClick(NPCRightClickEvent event) {
        if (event.getNPC() != campLeader || campLeader == null)
            return;
        new MenuArmyCamp(this).open(event.getClicker());
        makeCampLeaderSayNear("We have #p" + getNPCs().size() + "#s/#p" + getCapacity() + "#s warriors ready to go sir!", 8);
    }
}
