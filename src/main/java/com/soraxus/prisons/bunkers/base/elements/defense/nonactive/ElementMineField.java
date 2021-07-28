package com.soraxus.prisons.bunkers.base.elements.defense.nonactive;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A mine that blows up your enemies
 */
public class ElementMineField extends BunkerElement {
    private final List<UUID> fb = new ArrayList<>();
    private final Random random = new Random(System.currentTimeMillis());
    /**
     * How long this mine should persist for (in ticks)
     */
    private int persistentTicks = 200;
    private volatile boolean _exploding = false;

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementMineField(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    /**
     * Create a new mine
     *
     * @param bunker Bunker to make the mine in
     */
    public ElementMineField(Bunker bunker) {
        super(null, bunker);
        this.setHasExploded(false);
    }


    @Override
    public void onTick() {
        if (this.hasExploded()) {
            if (persistentTicks-- <= 0) {
                this.remove();
            }
        }
    }

    /**
     * Enable this mine
     */
    @Override
    protected void onEnable() {
        EventSubscriptions.instance.subscribe(this);
    }

    /**
     * Disable this mine
     */
    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribeAll(this);
    }

//    @Override
//    public BunkerElementType<? extends BunkerElement> getType() {
//        return BunkerElementType.DEFENSIVE.MINE_FIELD;
//    }

    /**
     * Get the shape of this mine
     *
     * @return IntVector2D.ONE
     */
    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    /**
     * Check if this mine has exploded
     *
     * @return <@code>true</@code> if it has
     */
    public synchronized boolean hasExploded() {
        return (boolean) this.getMeta().get("exploded");
    }

    /**
     * Set whether or not this mine has exploded
     *
     * @param value Boolean
     */
    private synchronized void setHasExploded(boolean value) {
        this.getMeta().set("exploded", value);
    }

    /**
     * Make this mine explode
     */
    public synchronized void explode() {
        if (this.hasExploded())
            return;
        this.setHasExploded(true);
        SpigotAsyncWorld world = new SpigotAsyncWorld(getBunker().getWorld().getBukkitWorld());

        //Explosion
        CuboidRegion region = new CuboidRegion(this.getLocation().subtract(0, 1, 0),
                this.getLocation().add(this.getShape().getX() * BunkerManager.TILE_SIZE_BLOCKS - 1, 3, this.getShape().getY() * BunkerManager.TILE_SIZE_BLOCKS - 1));
        Random random = new Random(System.currentTimeMillis());
        Map<IntVector3D, Integer> blocks = new HashMap<>();
        world.syncForAllInRegion(region, (p, b) -> {
            if (random.nextInt(2) == 0) {
                if (b == 0)
                    return;
                blocks.put(p, b);
            }
        }, false);

        //Build
        this.build(world);
        world.flush().thenAccept((n) -> {
            _exploding = true;
            try {
                blocks.forEach((p, b) -> {
                    World bukkitWorld = getBunker().getWorld().getBukkitWorld();
                    FallingBlock block = bukkitWorld.spawnFallingBlock(p.toBukkitVector().toLocation(bukkitWorld), Material.getMaterial(b & 0xFFF).getNewData((byte) (b >> 12)));
                    block.setDropItem(false);
                    block.setInvulnerable(true);
                    block.setHurtEntities(false);
                    block.setVelocity(new org.bukkit.util.Vector(random.nextDouble() * 2 - 1D, random.nextDouble() * 2 - 1D, random.nextDouble() * 2 - 1D));
                    fb.add(block.getUniqueId());
                });
            } finally {
                _exploding = false;
            }
        });
    }

    /**
     * Get the schematic of this mine
     *
     * @return Schematic
     */
    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        if (this.hasExploded()) {
            return BunkerSchematics.getWithoutThrow("mine-exploded-" + level + (destroyed ? "-destroyed" : ""));
        } else {
            return BunkerSchematics.getWithoutThrow("mine-" + level + (destroyed ? "-destroyed" : ""));
        }
    }

    /**
     * Get the maximum health of this mine
     *
     * @return 1
     */
    @Override
    public double getMaxHealth() {
        return 1;
    }

    @Override
    public String getName() {
        return "Mine Field";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.DEFENSIVE_MINE_FIELD;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    @EventSubscription
    private void onMove(PlayerMoveEvent event) {
        if (this.hasExploded() || random.nextInt(30) != 0)
            return;
        Vector3D pos = Vector3D.fromBukkitVector(event.getTo().toVector());
        if (isInsideThisElement(pos)) {
            this.explode();
        }
    }

    private boolean isInsideThisElement(Vector3D pos) {
        CuboidRegion region = new CuboidRegion(this.getLocation().toVector().subtract(new org.bukkit.util.Vector(0, 2, 0)).toLocation(getLocation().getWorld()), new Vector3D(this.getLocation().getX() + this.getShape().getX() * BunkerManager.TILE_SIZE_BLOCKS, this.getLocation().getY() + 4, this.getLocation().getZ() + this.getShape().getY() * BunkerManager.TILE_SIZE_BLOCKS).toBukkitVector().toLocation(this.getLocation().getWorld()));
        return region.contains(pos);
    }

    @EventSubscription
    private void onBlockFall(EntityChangeBlockEvent event) {
        if (fb.contains(event.getEntity().getUniqueId())) {
            ((FallingBlock) event.getEntity()).setDropItem(false);
            event.setCancelled(true);
            fb.remove(event.getEntity().getUniqueId());
        }
    }

}
