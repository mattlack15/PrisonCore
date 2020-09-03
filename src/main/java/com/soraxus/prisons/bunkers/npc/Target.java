package com.soraxus.prisons.bunkers.npc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;

@Getter
@AllArgsConstructor
public class Target {
    private AvailableTarget<?> target;
    private double targetDistance;

    public boolean conditionsMet(NPC npc) {
        return npc.getEntity().getLocation().distanceSquared(target.getImmediateLocation()) <= targetDistance * targetDistance;
    }

    public boolean exists() {
        return target.exists();
    }

    //Can be overridden
    public void apply(Targetter targetter) {
        target.apply(targetter);
    }
}
