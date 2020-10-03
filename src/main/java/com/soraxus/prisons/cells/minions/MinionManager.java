package com.soraxus.prisons.cells.minions;

import com.soraxus.prisons.bunkers.base.Meta;
import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.util.list.ElementableLockingList;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector3D;

import java.util.ArrayList;
import java.util.List;

public class MinionManager {
    @Getter
    private final Cell parent;

    private final ElementableLockingList<Minion> minions = new ElementableLockingList<>();

    public MinionManager(Cell parent) {
        this.parent = parent;
        try {
            this.load();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void update() {
        minions.forEach(Minion::update);
    }

    private void load() {
        byte[] b = parent.getMeta().get("minions");
        if (b == null)
            return;
        minions.getLock().perform(() -> {
            GravSerializer serializer = new GravSerializer(b);
            for (int i = 0, length = serializer.readInt(); i < length; i++) {
                Minion minion = new Minion(serializer, this);
                minions.addInternal(minion);
            }
        });
    }

    public void save(Meta cellMeta) {
        GravSerializer serializer = new GravSerializer();
        minions.getLock().perform(() -> {
            serializer.writeInt(minions.size());
            for (Minion minion : minions) {
                minion.serialize(serializer);
            }
        });
        cellMeta.set("minions", serializer.toByteArray());
    }

    public void destroyAll() {
        minions.getLock().perform(() -> minions.forEach(Minion::destroy));
    }

    public void spawnAll() {
        minions.getLock().perform(() -> minions.forEach(Minion::spawn));
    }

    public List<Minion> getMinions() {
        return minions.getLock().perform(() -> new ArrayList<>(minions));
    }

    public Minion byLocation(IntVector3D location) {
        return minions.getLock().perform(() -> minions.byFunction(location, Minion::getLocation));
    }

    public Minion createMinion(IntVector3D location) {
        Minion minion = new Minion(this, location);
        minions.getLock().perform(() -> minions.add(minion));
        return minion;
    }

    public boolean canPlace(IntVector3D location) {
        if (byLocation(location) != null)
            return false;
        return true;
    }

    void remove0(Minion minion) {
        minions.getLock().perform(() -> minions.remove(minion));
    }
}
