package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.Bunker;
import lombok.AccessLevel;
import lombok.Getter;
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
            this.npc.destroy();
            this.npc = null;
        }
    }

    public boolean isSpawned() {
        return this.npc != null;
    }

    private boolean targetApplied = false;

    public void tick() {
        if (!this.npc.isSpawned()) {
            Bukkit.broadcastMessage("This NPC is not spawned, or has despawned");
            return;
        }
        if (this.npc.getEntity() == null) {
            Bukkit.broadcastMessage("The NPC is spawned, but there is no entity. Has it died?");
            return;
        }
        if (this.getCurrentTarget() == null || !this.getCurrentTarget().exists()) {
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
            if (closest != null)
                setTarget(target(closest));
            return;
        }
        if (currentTarget.conditionsMet(npc)) {
            npc.getNavigator().setPaused(true);
            targetApplied = false;
        } else if (!npc.getNavigator().isNavigating() && !targetApplied) {
            targetApplied = true;
            currentTarget.apply(targetter);
            Bukkit.broadcastMessage("Applying target");
        }
    }

    /**
     * Kill the NPC, playing any death animations
     */
    public void die() {
        damage(2000);

        //TODO ...

        remove();
    }

    public void damage(double damage) {
        this.npc.setProtected(false);
        Entity ent = this.npc.getEntity();
        ((LivingEntity) ent).damage(damage);
        this.npc.setProtected(true);
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