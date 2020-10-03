package com.soraxus.prisons.bunkers.workers;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.Meta;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.util.BHoloTextBox;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.particles.ParticleShape;
import com.soraxus.prisons.util.particles.ParticleUtils;
import com.soraxus.prisons.util.time.DateUtils;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Task implements GravSerializable {
    @Getter
    private Worker worker;
    private String taskName;
    @Getter
    private Tile target;
    private long startTime = -1;
    private ParticleShape particles;
    private List<BHoloTextBox> textBoxs;
    private Meta meta;

    @Getter
    private boolean finished = false;

    @Getter
    private boolean started = false;

    public Task(GravSerializer serializer, Bunker bunker, Worker worker) throws IllegalStateException {
        this.taskName = serializer.readString();
        this.target = bunker.getTileMap().getTile(serializer.readObject());
        this.startTime = serializer.readLong();
        this.finished = serializer.readBoolean();
        this.started = serializer.readBoolean();
        this.meta = serializer.readObject();
        this.worker = worker;
        Synchronizer.synchronize(() -> {
            if(this.worker.getHut().isEnabled())
                this.setup();
        });
    }

    public Task(String name, Tile target, Worker worker) {
        if(target == null)
            throw new IllegalStateException("Target cannot be null");
        this.taskName = name;
        this.target = target;
        this.meta = new Meta();
        this.worker = worker;
        if (worker.getHut().getBunker().getWorkers().stream()
                .anyMatch(w -> {
                    if (w.getTask() == null) {
                        return false;
                    }
                    return w.getTask().getTarget().getPosition().equals(target.getPosition());
                })) {
            throw new IllegalStateException("bunker.worker.TileInUse");
        }
        this.setup();
    }

    private Vector3D toVec3D(IntVector3D vec) {
        return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
    }

    private void setup() {

        //Holograms
        this.textBoxs = new ArrayList<>();
        BunkerElement element = getTarget().getParent();

        //Side 1
        Tile tile = getTarget();
        Vector3D loc = new Vector3D(getTarget()
                .getBunker()
                .getTileMap()
                .getTileLocation(tile.getPosition()))
                .add(toVec3D(element.getShape().toIntVector3D(0).multiply(1, 0, 0)).divide(2D / BunkerManager.TILE_SIZE_BLOCKS))
                .add(0, 2.2, 0);
        Supplier<World> worldSupplier = () -> tile.getBunker().getWorld().getBukkitWorld();
        this.textBoxs.add(new BHoloTextBox(loc.toBukkitVector().toLocation(getTarget().getBunker().getWorld().getBukkitWorld()),
                0.3,
                false,
                worldSupplier));

        //Corner 2
        loc = new Vector3D(getTarget()
                .getBunker()
                .getTileMap()
                .getTileLocation(tile.getPosition()))
                .add(toVec3D(element.getShape().toIntVector3D(0).multiply(0, 0, 1)).divide(2D / BunkerManager.TILE_SIZE_BLOCKS))
                .add(0, 2.2, 0);
        this.textBoxs.add(new BHoloTextBox(loc.toBukkitVector().toLocation(getTarget().getBunker().getWorld().getBukkitWorld()),
                0.3,
                false,
                worldSupplier));

        //Corner 3
        loc = new Vector3D(getTarget()
                .getBunker()
                .getTileMap()
                .getTileLocation(tile.getPosition()))
                .add(toVec3D(element.getShape().toIntVector3D(0)).multiply(0.5D, 0D, 1D).multiply(BunkerManager.TILE_SIZE_BLOCKS))
                .add(0, 2.2, 0);
        this.textBoxs.add(new BHoloTextBox(loc.toBukkitVector().toLocation(getTarget().getBunker().getWorld().getBukkitWorld()),
                0.3,
                false,
                worldSupplier));

        //Corner 4
        loc = new Vector3D(getTarget()
                .getBunker()
                .getTileMap()
                .getTileLocation(tile.getPosition()))
                .add(toVec3D(element.getShape().toIntVector3D(0)).multiply(1D, 0D, 0.5D).multiply(BunkerManager.TILE_SIZE_BLOCKS))
                .add(0, 2.2, 0);
        this.textBoxs.add(new BHoloTextBox(loc.toBukkitVector().toLocation(getTarget().getBunker().getWorld().getBukkitWorld()),
                0.3,
                false,
                worldSupplier));

        this.textBoxs.forEach(t -> t.addLine(this.getCurrentText()));

        particles = ParticleUtils.createSquare(element.getBunker().getWorld().getBukkitWorld(), Particle.REDSTONE, Vector3D.fromBukkitVector(element.getLocation().toVector()), Vector3D.fromBukkitVector(element.getLocation().toVector()).add(element.getShape().getX() * BunkerManager.TILE_SIZE_BLOCKS, 0, element.getShape().getY() * BunkerManager.TILE_SIZE_BLOCKS));
    }

    /**
     * @throws IllegalStateException If the worker is not available for work
     */
    public synchronized boolean start() throws IllegalStateException {
        if(this.isStarted())
            return false;
        if(worker.getTask() != null && worker.getTask() != this) {
            throw new IllegalStateException("Worker not available!");
        } else if(worker.setTask(this)){
            this.startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void end() {
        synchronized (this) {
            this.finished = true;
        }
        this.textBoxs.forEach(BHoloTextBox::clear);
        try {
            this.getCallback().run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION while running Task Callback (" + this.getClass().getSimpleName() + ")");
        }
    }

    protected abstract Runnable getCallback();

    public String getCurrentText() {
        return "§7> " + taskName + " §7(§6" + DateUtils.readableDate(getRemainingTimeS(), true) + "§7)";
    }

    public long getRemainingTimeT() {
        return Math.max(startTime / 50 + getTimeNeededInt() - System.currentTimeMillis() / 50, 0);
    }

    public long getRemainingTimeS() {
        return (long) Math.ceil(getRemainingTimeT() / 20D);
    }

    public abstract void update();

    public abstract long getTimeNeeded();

    private long getTimeNeededInt() {
        return getTimeNeeded() / worker.getSpeed();
    }

    public void updateHolo() {
        if (getRemainingTimeT() <= 0) {
            end();
            return;
        }
        if(getRemainingTimeT() % 7 == 0) {
            particles.draw();
        }
        textBoxs.forEach(t -> t.setLine(0, getCurrentText()));
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(taskName);
        serializer.writeObject(target.getPosition());
        serializer.writeLong(startTime);
        serializer.writeBoolean(finished);
        serializer.writeBoolean(started);
        serializer.writeObject(meta);
    }
}
