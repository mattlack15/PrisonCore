package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.util.Scheduler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public abstract class AbstractBunkerNPCController {
    private final BunkerNPC parent;

    @Getter(value = AccessLevel.PROTECTED)
    private final Targetter targetter = new Targetter(this);

    private NPC npc;

    private UUID worldId;

    private Target currentTarget = null;

    private Bunker bunker;

    private NPCManager manager = null;

    @Setter
    private volatile boolean autoTargeting = true;

    public AbstractBunkerNPCController(BunkerNPC parent) {
        this.parent = parent;
    }

    public void spawnNPC(Bunker bunker, NPCManager manager, World world, Vector3D location) {
        this.bunker = bunker;
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "testname");
        this.npc.spawn(location.toBukkitVector().toLocation(world));
        this.worldId = world.getUID();
        this.manager = manager;
        this.onSpawn(npc);
        manager.addNPC(parent);
    }

    public abstract void onSpawn(NPC npc);

    /**
     * Get the NPC ID
     */
    public UUID getID() {
        return this.npc.getUniqueId();
    }

    /**
     * Remove NPC without playing any death animation
     */
    public void remove() {
        if (this.npc != null) {
            this.getBunker().getNpcManager().removeNPC(this.getID());
            this.npc.despawn();
            this.npc.destroy();
            this.npc = null;
        }
    }

    public boolean isSpawned() {
        return this.npc != null;
    }

    private final AtomicBoolean targetApplied = new AtomicBoolean(false);

    public void retarget() {
        List<AvailableTarget<?>> targets = this.getAvailableTargets();
        AvailableTarget<?> closest = null;
        double closestDist = -1D;
        for (AvailableTarget<?> target : targets) {
            if (target.get().equals(this.getParent()))
                continue;
            double dist = target.getImmediateLocation().distanceSquared(this.getLocation().toBukkitVector().toLocation(this.getWorld()));
            if (dist < closestDist || closestDist == -1D) {
                closestDist = dist;
                closest = target;
            }
        }

        if (this.getCurrentTarget() != null && closest != null)
            if (closest.get() == this.getCurrentTarget().getTarget().get())
                return;

        if (closest != null)
            setTarget(target(closest));
        targetApplied.set(false);
    }

    public void tick() {
        if (!this.npc.isSpawned()) {
            return;
        }
        if (autoTargeting && (this.getCurrentTarget() == null || !this.getCurrentTarget().exists())) {
            retarget();
            return;
        }
        if (currentTarget != null && currentTarget.conditionsMet(npc) && npc.getNavigator().isNavigating()) {
            npc.getNavigator().cancelNavigation();
            targetApplied.set(false);
        } else if (currentTarget != null && targetApplied.compareAndSet(false, true)) {
            currentTarget.apply(targetter);
        }
    }

    /**
     * Kill the NPC, playing any death animations
     */
    public void die() {
        LivingEntity ent = (LivingEntity) this.npc.getEntity();
        ent.damage(Double.MAX_VALUE);
        Scheduler.scheduleSyncDelayedTask(this::remove, 1);
    }

    public void damage(double damage) {
        this.npc.setProtected(false);
        Entity ent = this.npc.getEntity();
        boolean died = ((LivingEntity) ent).getHealth() - damage <= 0;
        ((LivingEntity) ent).damage(damage);
        this.npc.setProtected(true);
        if (died) this.die();
    }

    public World getWorld() {
        return Bukkit.getWorld(worldId);
    }

    public Vector3D getLocation() {
        if (this.npc == null || !this.npc.isSpawned()) {
            return null;
        }
        return Vector3D.fromBukkitVector(this.npc.getEntity().getLocation().toVector());
    }

    public void setTarget(Target target) {
        this.currentTarget = target;
    }

    public Location getCurrentTargetLocation() {
        return this.npc == null ? null : this.npc.getNavigator().getTargetAsLocation();
    }

    public abstract Target target(AvailableTarget<?> availableTarget);

    public abstract List<AvailableTarget<?>> getAvailableTargets();
}