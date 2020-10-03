package com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
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

    public synchronized List<BunkerNPC> getNPCs() {
        List<BunkerNPC> npcList = getMeta().getObject("npcs");
        if (npcList == null) {
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
        Synchronizer.synchronize(() -> {
            campLeader = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, ChatColor.BLUE + "Camp Leader");
            campLeader.spawn(CAMP_LEADER_POS.toBukkitVector().toLocation(getBunker().getWorld().getBukkitWorld()).add(this.getLocation()));
        });
    }

    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribe(this);
        Synchronizer.synchronize(() -> {
            campLeader.despawn();
            campLeader.destroy();
        });
    }

    public void makeCampLeaderSayNear(String message) {
        if (getBunker().getWorld() != null) {
            List<Player> players = getBunker().getWorld().getBukkitWorld().getPlayers();
            CuboidRegion region = getBoundingRegion(20);
            for (Player player : players) {
                if (region.contains(Vector3D.fromBukkitVector(player.getLocation().toVector()))) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCamp Leader &f&l> &7" + message));
                }
            }
        }
    }

    public void makeCampLeaderSayNear(String message, double distance) {
        if (getBunker().getWorld() != null) {
            List<Player> players = getBunker().getWorld().getBukkitWorld().getPlayers();
            CuboidRegion region = getBoundingRegion(20);
            for (Player player : players) {
                if (region.smallestDistance(Vector3D.fromBukkitVector(player.getLocation().toVector())) <= distance) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCamp Leader &f&l> &7" + message));
                }
            }
        }
    }

    public void makeCampLeaderSayGang(String message) {
        List<Player> players = getBunker().getGang().getMembers().stream().map((m) -> Bukkit.getPlayer(m.getMember())).collect(Collectors.toList());
        for (Player player : players) {
            if (player == null)
                return;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCamp Leader &f&l> &7" + message));
        }
    }

    @EventSubscription
    private void onLeaderClick(NPCRightClickEvent event) {
        if (event.getNPC() != campLeader || campLeader == null)
            return;
        new MenuArmyCamp(this).open(event.getClicker());
        makeCampLeaderSayNear("We have &d" + getNPCs().size() + "&5/&d" + getCapacity() + "&7 warriors ready to go sir!", 8);
    }
}
