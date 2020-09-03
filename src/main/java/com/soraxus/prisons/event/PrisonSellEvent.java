package com.soraxus.prisons.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PrisonSellEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Getter
    private Player player;
    @Getter
    private ItemStack item;
    @Getter
    @Setter
    private double multiplier;

    public PrisonSellEvent(Player player, ItemStack item, double multiplier) {
        this.player = player;
        this.item = item;
        this.multiplier = multiplier;
    }
}
