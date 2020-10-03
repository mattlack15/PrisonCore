package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerPlayerTeleportEvent extends Event {
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
    @Getter
    private final Player player;

    public BunkerPlayerTeleportEvent(Player player, Bunker bunker) {
        this.player = player;
        this.bunker = bunker;
    }

}
