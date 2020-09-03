package com.soraxus.prisons.bunkers.npc.combat;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BunkerNPCAbility {
    @Getter
    private final AbstractBunkerNPCController parent;
    @Getter
    private final AtomicInteger cooldown = new AtomicInteger();
    public BunkerNPCAbility(String name, AbstractBunkerNPCController parent) {
        this.parent = parent;
    }

    public abstract String getDescription();

    public abstract void use();

    public abstract boolean canUse();

    public abstract boolean isPassive();

    public abstract int cooldownTicks();
}
