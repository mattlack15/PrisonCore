package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerElementDamageEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Getter
    private final BunkerElement element;

    private ElementDrop drop;

    private double damage;

    public BunkerElementDamageEvent(BunkerElement element, double damage, ElementDrop drop) {
        super(!Bukkit.isPrimaryThread());
        this.element = element;
        this.damage = damage;
        this.drop = drop;
    }

    public synchronized ElementDrop getDrop() {
        return this.drop;
    }

    public synchronized void setDrop(ElementDrop drop) {
        this.drop = drop;
    }

    public synchronized double getDamage() {
        return this.damage;
    }

    public synchronized void setDamage(double damage) {
        this.damage = damage;
    }

}
