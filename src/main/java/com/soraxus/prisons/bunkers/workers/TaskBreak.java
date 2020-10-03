package com.soraxus.prisons.bunkers.workers;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import net.ultragrav.serializer.GravSerializer;

public class TaskBreak extends Task {

    private double health = 0D;

    public TaskBreak(GravSerializer serializer, Bunker bunker, Worker worker) {
        super(serializer, bunker, worker);
    }

    public TaskBreak(Tile tile, Worker worker) {
        super("Breaking", tile.getBunker().getTileMap().getTile(tile.getParent().getPosition()), worker);
    }

    @Override
    protected Runnable getCallback() {
        return () -> {
            getTarget().getParent().remove();
        };
    }

    @Override
    public synchronized boolean start() throws IllegalStateException {
        if(super.start()) {
            health = getTarget().getParent().getHealth();
            getTarget().getParent().disable();
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        double damage = getTarget().getParent().getHealth() / getRemainingTimeT();

        ElementDrop drop = getTarget().getParent().getDropForDamage(getTarget().getParent().getHealth() / (double) getRemainingTimeT());

        if (getTarget().getParent().getHealth() - damage > 0) {
            getTarget().getParent().damage(getTarget().getParent().getHealth() / (double) getRemainingTimeT());
        } else {
            getTarget().getParent().remove();
        }
        if (drop != null)
            drop.apply(getTarget().getBunker());
    }

    @Override
    public long getTimeNeeded() {
        return (long) (health / getWorker().getSpeed() * 20);
    }
}
