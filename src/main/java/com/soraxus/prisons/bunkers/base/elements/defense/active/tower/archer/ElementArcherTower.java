package com.soraxus.prisons.bunkers.base.elements.defense.active.tower.archer;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.defense.active.ActiveDefenseElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.metadata.EventMetadata;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.ultragrav.asyncworld.nbt.Tag;
import net.ultragrav.asyncworld.nbt.TagString;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ElementArcherTower extends ActiveDefenseElement {
    private final List<NPC> archers = new ArrayList<>();
    private int ticks = 0;

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementArcherTower(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementArcherTower(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    protected void onEnable() {
        //Search for archer positions
        List<Vector3D> npcPositions = new ArrayList<>();

        Schematic schematic = getSchematic();
        schematic.getTiles().forEach((p, t) -> {
            if ((schematic.getBlockAt(p.getX(), p.getY(), p.getZ()) & 0xFFF) == Material.SIGN_POST.getId()) {
                Tag textTag = t.getData().get("Text1");
                if (textTag instanceof TagString) {
                    String text = ((TagString) textTag).getData();
                    if (text.contains("[NPC]"))
                        npcPositions.add(new Vector3D(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D));
                }
            }
        });

        Synchronizer.synchronize(() -> {
            if(!this.isEnabled())
                return;
            if (!archers.isEmpty()) {
                archers.forEach(NPC::destroy);
                archers.clear();
            }
            for (int i = 0; i < 4; i++) {
                Vector3D position = npcPositions.get(i);

                position.floor().toBukkitVector().toLocation(this.getBunker().getWorld().getBukkitWorld())
                        .getBlock().setType(Material.AIR);

                archers.add(CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "&eArcher"));
                archers.get(i).spawn(getLocation().add(position.toBukkitVector()));
                NPC npc = archers.get(i);
                npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.BOW));
            }
        });
    }

    @Override
    protected void onDisable() {
        Synchronizer.synchronize(() -> {
            archers.forEach(NPC::destroy);
            archers.clear();
        });
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public double getMaxHealth() {
        return 250 * getLevel();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.DEFENSIVE_ARCHER_TOWER;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    @Override
    public void onTick() {
        if (!isDefendingMatchActive())
            return;

        if (++ticks < 8) {
            return;
        }
        ticks = 0;

        //Pick target
        List<AvailableTarget<?>> targetList = getBunker().getDefendingMatch().getDefenderTargets();
        AvailableTarget<?> target = null;
        int range = getLevel() * 5 + 15;
        Location ourLocation = getLocation().add(3.5, 0, 3.5);
        for (AvailableTarget<?> availableTarget : targetList) {
            Location location = availableTarget.getImmediateLocation();
            if (location.distanceSquared(ourLocation) <= range * range) {
                target = availableTarget;
                break;
            }
        }

        if (target == null)
            return; //Could not find a target

        //Pick archer
        NPC archer = null;
        double archerDistance = -1;
        Location targetLocation = target.getImmediateLocation();

        for (NPC npc : archers) {
            if (npc.getStoredLocation().distanceSquared(targetLocation) <= archerDistance || archerDistance == -1) {
                archer = npc;
                archerDistance = npc.getStoredLocation().distanceSquared(targetLocation);
            }
        }

        assert archer != null;

        //Fire arrow
        Location fireLocation = ((LivingEntity) archer.getEntity()).getEyeLocation();
        Vector dir = targetLocation.toVector().subtract(fireLocation.toVector()).add(new org.bukkit.util.Vector(0, 1.8, 0));
        Arrow arrow = archer.getStoredLocation().getWorld().spawnArrow(fireLocation.add(dir.normalize().multiply(2.5)), dir, 2, 0);
        EventMetadata value = new EventMetadata() {
            @EventSubscription
            private void onHit(ProjectileHitEvent event) {
                if (event.getEntity().getUniqueId().equals(arrow.getUniqueId())) {
                    event.getEntity().remove();
                    NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
                    if (npc != null) {
                        BunkerNPC bunkerNPC = getBunker().getDefendingMatch().getNpcManager().getNpc(arrow);
                        if (bunkerNPC != null) {
                            bunkerNPC.damage(8 * getLevel());
                        }
                    }
                }
            }
        };
        arrow.setMetadata("spc::event", value);
    }
}
