package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerSaveEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final Bunker bunker;
    private final boolean asynchronous;

    /**
     * May be asynchronous
     */
    public BunkerSaveEvent(Bunker bunker, boolean asynchronous) {
        super(asynchronous);
        this.bunker = bunker;
        this.asynchronous = asynchronous;
    }

    /**
     * Gets the bunker that was saved
     * @return the bunker that was saved
     */
    public Bunker getBunker() {
        return bunker;
    }
}
