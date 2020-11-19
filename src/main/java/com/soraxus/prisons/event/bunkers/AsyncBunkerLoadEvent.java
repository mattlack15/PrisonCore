package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncBunkerLoadEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final Bunker bunker;

    public AsyncBunkerLoadEvent(Bunker bunker) {
        super(true);
        this.bunker = bunker;
    }

    /**
     * Gets the bunker that was loaded asynchronously
     *
     * @return the loaded bunker
     */
    public Bunker getBunker() {
        return bunker;
    }
}
