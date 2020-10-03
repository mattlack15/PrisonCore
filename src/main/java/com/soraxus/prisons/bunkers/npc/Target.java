package com.soraxus.prisons.bunkers.npc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;

@Getter
@AllArgsConstructor
public class Target {
    private AvailableTarget<?> target;
    private double targetDistance;


    public boolean conditionsMet(NPC npc) {
        if (!exists())
            return true;
        if (target instanceof ElementAvailableTarget) {
            CuboidRegion region = ((ElementAvailableTarget) target).get().getBoundingRegion(256);
            double distance = region.smallestDistance(Vector3D.fromBukkitVector(npc.getEntity().getLocation().toVector()));
            return distance <= targetDistance;
        } else {
            return npc.getEntity().getLocation().distanceSquared(target.getImmediateLocation()) <= targetDistance * targetDistance;
        }
    }

    public boolean exists() {
        return target.exists();
    }

    //Can be overridden
    public void apply(Targetter targetter) {
        target.apply(targetter);
    }
}
