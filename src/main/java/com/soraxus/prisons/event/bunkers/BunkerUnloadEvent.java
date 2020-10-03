package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerUnloadEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Getter
    private final Bunker bunker;

    public BunkerUnloadEvent(Bunker bunker, boolean async) {
        super(async);
        this.bunker = bunker;
    }
}
