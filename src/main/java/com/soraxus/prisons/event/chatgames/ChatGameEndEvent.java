package com.soraxus.prisons.event.chatgames;

import com.soraxus.prisons.chatgames.ChatGame;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.UUID;

public class ChatGameEndEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Getter
    private ChatGame game;

    @Getter
    private Map<UUID, Long> winners;

    public ChatGameEndEvent(ChatGame game, Map<UUID, Long> winners) {
        this.game = game;
        this.winners = winners;
    }
}
