package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.matchmaking.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerMatchStartEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final Match match;
    public BunkerMatchStartEvent(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }
}
