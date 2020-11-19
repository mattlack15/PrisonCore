package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.workers.Task;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerTaskStartEvent extends Event {
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
    private final Task task;

    @Getter
    private final Tile tile;

    public BunkerTaskStartEvent(Task task) {
        super(!Bukkit.isPrimaryThread());
        if (task == null || task.getWorker() == null) {
            throw new IllegalStateException("Task, or worker was null!");
        }
        this.task = task;
        this.tile = task.getTarget();
        this.bunker = task.getWorker().getHut().getBunker();
    }
}
