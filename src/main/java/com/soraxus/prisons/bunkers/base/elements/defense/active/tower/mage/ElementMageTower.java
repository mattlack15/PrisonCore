package com.soraxus.prisons.bunkers.base.elements.defense.active.tower.mage;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.defense.active.ActiveDefenseElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.util.Synchronizer;
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
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ElementMageTower extends ActiveDefenseElement {

    private final List<NPC> mages = new ArrayList<>();

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementMageTower(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementMageTower(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    protected void onEnable() {

        //Search for mage positions
        List<Vector3D> npcPositions = new ArrayList<>();

        Schematic schematic = getSchematic();
        schematic.getTiles().forEach((p, t) -> {
            if((schematic.getBlockAt(p.getX(), p.getY(), p.getZ()) & 0xFFF) == Material.SIGN_POST.getId()) {
                Tag textTag = t.getData().get("Text1");
                if(textTag instanceof TagString) {
                    String text = ((TagString)textTag).getData();
                    if(text.contains("[NPC]"))
                        npcPositions.add(new Vector3D(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D));
                }
            }
        });



        Synchronizer.synchronize(() -> {
            if(!mages.isEmpty()) {
                mages.forEach(NPC::destroy);
                mages.clear();
            }
            for(int i = 0; i < 4; i++) {
                Vector3D position = npcPositions.get(i);

                position.floor().toBukkitVector().toLocation(this.getBunker().getWorld().getBukkitWorld())
                        .getBlock().setType(Material.AIR);

                mages.add(CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "&dMage"));
                mages.get(i).spawn(getLocation().add(position.toBukkitVector()));
                NPC npc = mages.get(i);
                npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.BLAZE_ROD));
            }
        });
    }
    @Override
    protected void onDisable() {
        Synchronizer.synchronize(() -> {
            mages.forEach(NPC::destroy);
            mages.clear();
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

    private int ticks = 0;

    @Override
    public void onTick() {
        if(!isDefendingMatchActive())
            return;
        if(++ticks >= 8) {
            ticks = 0;

            //Pick target
            List<AvailableTarget<?>> targetList = getBunker().getDefendingMatch().getDefenderTargets();
            AvailableTarget<?> target = null;
            int range = getLevel() * 5 + 15;
            Location ourLocation = getLocation().add(3.5, 0, 3.5);
            for (AvailableTarget<?> availableTarget : targetList) {
                Location location = availableTarget.getImmediateLocation();
                if(location.distanceSquared(ourLocation) <= range * range) {
                    target = availableTarget;
                    break;
                }
            }

            if(target == null)
                return; //Could not find a target

            //Pick mage
            NPC mage = null;
            double archerDistance = -1;
            Location targetLocation = target.getImmediateLocation();

            for (NPC npc : mages) {
                if(npc.getStoredLocation().distanceSquared(targetLocation) <= archerDistance || archerDistance == -1) {
                    mage = npc;
                    archerDistance = npc.getStoredLocation().distanceSquared(targetLocation);
                }
            }

            assert mage != null;

            //Fire arrow
            Location fireLocation = ((LivingEntity)mage.getEntity()).getEyeLocation();
            Vector dir = targetLocation.toVector().subtract(fireLocation.toVector()).add(new Vector(0, 1.8, 0));
            dir = dir.normalize();
            Fireball fireball = mage.getStoredLocation().getWorld().spawn(fireLocation.add(dir.normalize().multiply(2.5)), Fireball.class);
            fireball.setDirection(dir);
            fireball.setVelocity(dir);
        }
    }
}
