package com.soraxus.prisons.bunkers.base.elements.entertainment;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ElementPlot extends BunkerElement {
    private final AtomicBoolean edited = new AtomicBoolean(false);
    private int i = 0;

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementPlot(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementPlot(Bunker bunker) {
        super(null, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(3, 3);
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow("plot-" + level);
    }

    public Map<Integer, Schematic> getHeldSchematics() {
        return getMeta().getOrSet("schematics", new ConcurrentHashMap<>());
    }

    public int getCurrentSchematicIndex() {
        return getMeta().getOrSet("current", 0);
    }

    public synchronized void save() {
        CuboidRegion region = getBuildableRegion();
        Schematic schematic = new Schematic(new IntVector3D(0, 0, 0), new SpigotAsyncWorld(getBunker().getWorld().getBukkitWorld()), region);
        getHeldSchematics().put(getCurrentSchematicIndex(), schematic);
    }

    public CuboidRegion getBuildableRegion() {
        CuboidRegion region = this.getBoundingRegion(21);
        region.contract(new Vector3D(3, 0, 3), new Vector3D(-3, 0, -3));
        return region;
    }

    @Override
    protected void onSaveAsync(GravSerializer serializer) {
        save();
    }

    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribe(this);
    }

    @Override
    protected void onEnable() {
        EventSubscriptions.instance.subscribe(this);
    }

    @Override
    public void onTick() {
        i++;
        if (i >= 2400 && edited.get()) {
            i = 0;
            new Thread(() -> {
                synchronized (this) {
                    if (!this.isEnabled())
                        return;
                    edited.set(false);
                    this.save();
                }
            }).start();
        }
    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        Vector3D pos = Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector());
        if (getBuildableRegion().contains(pos)) {
            edited.set(true);
        } else if (getBoundingRegion(256).contains(pos)) {
            event.setCancelled(true);
        }
    }

    @EventSubscription
    private void onBuild(BlockPlaceEvent event) {
        Vector3D pos = Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector());
        if (getBuildableRegion().contains(pos)) {
            edited.set(true);
        } else if (getBoundingRegion(256).contains(pos)) {
            event.setCancelled(true);
        }
    }

    @Override
    protected void onBuild(AsyncWorld world) {
        if (getHeldSchematics().size() != 0)
            world.pasteSchematic(
                    getHeldSchematics().get(getCurrentSchematicIndex()),
                    IntVector3D.fromBukkitVector(getLocation().toVector()).add(3, 0, 3)
            );
    }

    @Override
    public double getMaxHealth() {
        return 50;
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ENTERTAINMENT_PLOT;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
