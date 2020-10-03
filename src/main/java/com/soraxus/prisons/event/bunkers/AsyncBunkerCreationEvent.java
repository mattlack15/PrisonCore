package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncBunkerCreationEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final Bunker bunker;
    public AsyncBunkerCreationEvent (Bunker bunker) {
        super(true);
        this.bunker = bunker;
    }

    /**
     * Gets th bunker that has been created asynchronously
     * @return The created bunker
     */
    public Bunker getBunker() {
        return this.bunker;
    }
}
