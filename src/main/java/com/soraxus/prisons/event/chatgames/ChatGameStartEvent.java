package com.soraxus.prisons.event.chatgames;

import com.soraxus.prisons.chatgames.ChatGame;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChatGameStartEvent extends Event {
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

    public ChatGameStartEvent(ChatGame game) {
        this.game = game;
    }
}
