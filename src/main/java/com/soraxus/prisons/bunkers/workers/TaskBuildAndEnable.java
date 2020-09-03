package com.soraxus.prisons.bunkers.workers;

import com.soraxus.prisons.bunkers.base.ElementGenerationSettings;
import com.soraxus.prisons.bunkers.base.Tile;

public class TaskBuildAndEnable extends Task {
    public TaskBuildAndEnable(Tile tile, Worker worker) {
        super("Building", tile, worker);
        tile.getParent().setGenerationSettings(new ElementGenerationSettings(false, false));
    }

    @Override
    protected Runnable getCallback() {
        return () -> {
            try {
                getTarget().getParent().build();
            } catch(Exception e) {
                e.printStackTrace();
            }
            getTarget().getParent().enable();
        };
    }

    @Override
    public void update() {

    }

    @Override
    public long getTimeNeeded() {
        return getTarget().getParent().getType().getBuildTimeTicks(getTarget().getParent().getLevel());
    }
}
