package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.workers.Task;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BunkerTaskEndEvent extends Event {
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

    private final boolean successful;

    public BunkerTaskEndEvent(Task task, boolean successful) {
        super(!Bukkit.isPrimaryThread());
        if (task == null || task.getWorker() == null) {
            throw new IllegalStateException("Task, or worker was null!");
        }
        this.task = task;
        this.successful = successful;
        this.bunker = task.getWorker().getHut().getBunker();
    }

    public boolean wasSuccessful() {
        return successful;
    }

}
