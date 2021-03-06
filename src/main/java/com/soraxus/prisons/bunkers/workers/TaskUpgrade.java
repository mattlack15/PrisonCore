package com.soraxus.prisons.bunkers.workers;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.Tile;
import net.ultragrav.serializer.GravSerializer;

public class TaskUpgrade extends Task {

    public TaskUpgrade(GravSerializer serializer, Bunker bunker, Worker worker) {
        super(serializer, bunker, worker);
    }

    public TaskUpgrade(Tile tile, Worker worker) throws IllegalStateException {
        super("Upgrading", tile, worker);
        if (tile.getParent().getLevel() == tile.getParent().getMaxLevel())
            throw new IllegalStateException("Element has reached max level!");
    }

    @Override
    protected Runnable getCallback() {
        return () -> {
            this.getTarget().getParent().setLevel(this.getTarget().getParent().getLevel() + 1);
            this.getTarget().getParent().build();
            this.getTarget().getParent().setHealth(this.getTarget().getParent().getMaxHealth());
            this.getTarget().getParent().enable();
        };
    }

    @Override
    public void update() {

    }

    @Override
    public boolean start() throws IllegalStateException {
        if (super.start()) {
            this.getTarget().getParent().disable();
            return true;
        }
        return false;
    }

    @Override
    public long getTimeNeeded() {
        return getTarget().getParent().getType().getBuildTimeTicks(getTarget().getParent().getLevel() + 1);
    }
}
