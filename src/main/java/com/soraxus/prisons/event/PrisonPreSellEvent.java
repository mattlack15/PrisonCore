package com.soraxus.prisons.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PrisonPreSellEvent extends Event {
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
    private List<ItemStack> items;
    @Getter
    @Setter
    private double multiplier;

    public PrisonPreSellEvent(Player player, List<ItemStack> items, double multiplier) {
        this.player = player;
        this.items = items;
        this.multiplier = multiplier;
    }
}
