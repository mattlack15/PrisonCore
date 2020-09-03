package com.soraxus.prisons.bunkers.workers;

import com.soraxus.prisons.bunkers.base.Tile;

public class TaskUpgrade extends Task {
    public TaskUpgrade(Tile tile, Worker worker) throws IllegalStateException {
        super("Upgrading", tile, worker);
        if(tile.getParent().getLevel() == tile.getParent().getMaxLevel())
            throw new IllegalStateException("Element has reached max level!");
    }

    @Override
    protected Runnable getCallback() {
        return () -> {
            this.getTarget().getParent().setLevel(this.getTarget().getParent().getLevel()+1);
            this.getTarget().getParent().build();
            this.getTarget().getParent().enable();
        };
    }

    @Override
    public void update() {

    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        this.getTarget().getParent().disable();
    }

    @Override
    public long getTimeNeeded() {
        return getTarget().getParent().getType().getBuildTimeTicks(getTarget().getParent().getLevel()+1); //TODO change
    }
}
