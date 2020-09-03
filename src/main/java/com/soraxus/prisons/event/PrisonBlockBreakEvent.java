package com.soraxus.prisons.event;

import com.soraxus.prisons.util.items.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.utils.Vector3D;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PrisonBlockBreakEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Getter
    private Vector3D location;
    @Getter
    private Player player;
    @Getter
    private int block;
    @Getter
    @Setter
    private int amount = 1;

    public PrisonBlockBreakEvent(Player player, Vector3D location, int combinedId) {
        this.player = player;
        this.location = location;
        this.block = combinedId;
    }

    public ItemStack getToBeGiven() {
        ItemStack broken = ItemUtils.fromBlock(this.getBlock(), 1);
        //TODO
        return broken;
    }
}
