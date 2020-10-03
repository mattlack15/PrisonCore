package com.soraxus.prisons.bunkers.base.elements.research;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
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

import java.util.List;
import java.util.stream.Collectors;

public class ElementLaboratory extends BunkerElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementLaboratory(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementLaboratory(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(2, 2);
    }

    @Override
    public double getMaxHealth() {
        return 10;
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.RESEARCH_LABORATORY;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    private NPC madScientist = null;

    private static final Vector3D SCIENTIST_POS = new Vector3D(7.5, 0, 7.5);

    @Override
    protected void onEnable() {
        EventSubscriptions.instance.subscribe(this);
        Synchronizer.synchronize(() -> {
            madScientist = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, ChatColor.AQUA + "Mad Scientist");
            madScientist.spawn(SCIENTIST_POS.toBukkitVector().toLocation(getBunker().getWorld().getBukkitWorld()).add(this.getLocation()));
        });
    }

    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribe(this);
        Synchronizer.synchronize(() -> {
            madScientist.despawn();
            madScientist.destroy();
        });
    }

    public void makeScientistSayNear(String message) {
        if(getBunker().getWorld() != null) {
            List<Player> players = getBunker().getWorld().getBukkitWorld().getPlayers();
            CuboidRegion region = getBoundingRegion(20);
            for (Player player : players) {
                if (region.contains(Vector3D.fromBukkitVector(player.getLocation().toVector()))) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bMad Scientist &f&l> &7" + message));
                }
            }
        }
    }

    public void makeScientistSayNear(String message, double distance) {
        if(getBunker().getWorld() != null) {
            List<Player> players = getBunker().getWorld().getBukkitWorld().getPlayers();
            CuboidRegion region = getBoundingRegion(20);
            for (Player player : players) {
                if (region.smallestDistance(Vector3D.fromBukkitVector(player.getLocation().toVector())) <= distance) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bMad Scientist &f&l> &7" + message));
                }
            }
        }
    }

    public void makeScientistSayGang(String message) {
        List<Player> players = getBunker().getGang().getMembers().stream().map((m) -> Bukkit.getPlayer(m.getMember())).collect(Collectors.toList());
        for (Player player : players) {
            if(player == null)
                return;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bMad Scientist &f&l> &7" + message));
        }
    }

    @EventSubscription
    private void onLeaderClick(NPCRightClickEvent event) {
        if(event.getNPC() != madScientist || madScientist == null)
            return;
        new MenuLaboratory(this).open(event.getClicker());
    }
}
