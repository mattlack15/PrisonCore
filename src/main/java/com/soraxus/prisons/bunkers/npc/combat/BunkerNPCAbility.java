package com.soraxus.prisons.bunkers.npc.combat;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import lombok.Getter;
import org.bukkit.Location;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BunkerNPCAbility {
    @Getter
    private final AbstractBunkerNPCController parent;
    @Getter
    private final AtomicInteger cooldown = new AtomicInteger(cooldownTicks());
    @Getter
    private final String name;
    public BunkerNPCAbility(String name, AbstractBunkerNPCController parent) {
        this.parent = parent;
        this.name = name;
    }

    public abstract String getDescription();

    public abstract void use();

    public boolean canUse() {
        return getParent().getCurrentTarget() != null && getParent().getCurrentTarget().exists() && getParent().getCurrentTarget().conditionsMet(getParent().getNpc());
    }

    public abstract boolean isPassive();

    public abstract int cooldownTicks();

    public void tick() {};

    public Location getTargetImmediateLocation() {
        return this.parent.getCurrentTarget().getTarget().getImmediateLocation();
    }
}
