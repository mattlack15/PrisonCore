package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.bunkers.base.elements.BunkerElementFlag;
import com.soraxus.prisons.bunkers.base.elements.animation.DamageAnimationHandler;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.gui.tile.MenuTileOccupied;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.event.bunkers.BunkerElementDamageEvent;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public abstract class BunkerElement implements GravSerializable {
    public static final int DEFAULT_MAX_LEVEL = 10;

    /**
     * The parent bunker
     */
    private final Bunker bunker;
    /**
     * Flag lock
     */
    private final ReentrantLock flagListLock = new ReentrantLock(true);
    /**
     * Meta for this bunker element
     * internal.* is reserved for internal usage
     */
    private Meta meta = new Meta();

    /**
     * The ID of this element
     */
    private UUID id = UUID.randomUUID();

    /**
     * The position of this element in the grid
     */
    @Setter(AccessLevel.PROTECTED)
    private IntVector2D position;

    /**
     * The level of this element
     */
    private int level = 1; //starts at 1

    @Setter
    private int rotation = 0;
    @Getter
    private double health = getMaxHealth();
    @Getter
    private boolean enabled = false;
    @Getter
    private boolean destroyed = false;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean built = false;
    private DamageAnimationHandler damageAnimationHandler = new DamageAnimationHandler(this, 5, 120);
    @Setter
    private ElementGenerationSettings generationSettings = new ElementGenerationSettings(true, true);
    private volatile boolean _beingRemoved = false;

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public BunkerElement(GravSerializer serializer, Bunker bunker) {
        // Set the parent bunker field
        this.bunker = bunker;

        // Check if the serializer is null
        if (serializer == null) {
            //First creation, not being deserialized

            // Add default flags
            this.addFlag(BunkerElementFlag.PROTECTED);

            // Return to avoid deserialization attempt
            return;
        }

        //Deserialize
        id = serializer.readUUID(); // Read element id
        position = serializer.readObject(); // Read element position
        level = serializer.readInt(); // Read element level
        health = serializer.readDouble(); // Read element health
        destroyed = serializer.readBoolean(); // Read whether or not this element is destroyed

        // Set generation settings
        this.setGenerationSettings(new ElementGenerationSettings(serializer.readBoolean(), serializer.readBoolean()));

        // Read element meta
        meta = serializer.readObject();

        // Get async serializer
        GravSerializer serializer1 = serializer.readSerializer();

        // Try to catch exceptions
        try {
            // Call async onLoad method with the remaining serialized data
            this.onLoadAsync(serializer1);
        } catch (Exception e) {
            // Print caught exception
            e.printStackTrace();

            // Message exception to devs
            ModuleBunkers.messageDevs("Exception thrown during loading of element of type " + this.getType().getName());
        }
    }

    /**
     * Load an element from a serializer
     *
     * @param serializer Serializer
     * @param bunker     Parent bunker object
     * @return Object that extends BunkerElement
     */
    public static BunkerElement deserialize(GravSerializer serializer, Bunker bunker) {
        try {
            Class<?> clazz = Class.forName(serializer.readString());
            if (!BunkerElement.class.isAssignableFrom(clazz)) {
                System.out.println("BunkerElement.get(GravSerializer, Bunker) -> Unable to attain Bunker Element type from class " + clazz.getName());
                return null;
            }
            return (BunkerElement) clazz.getConstructor(GravSerializer.class, Bunker.class).newInstance(serializer, bunker);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called when this element is deserialized
     *
     * @param readSerializer a provided serializer in which you can manually load things without using this element's meta, please note
     *                       that using meta is recommended
     */
    protected void onLoadAsync(GravSerializer readSerializer) {
    }

    /**
     * Serialize this element into a serializer
     *
     * @param serializer Serializer
     */
    public final void serialize(GravSerializer serializer) {
        GravSerializer serializer1 = new GravSerializer();
        try {
            this.onSaveAsync(serializer1);
        } catch (Exception e) {
            e.printStackTrace();
            ModuleBunkers.messageDevs("Exception thrown during saving of element of type " + getType().getName());
        }

        serializer.writeString(getClass().getName());
        serializer.writeUUID(id);
        serializer.writeObject(position);
        serializer.writeInt(level);
        serializer.writeDouble(health);
        serializer.writeBoolean(destroyed);
        serializer.writeBoolean(this.getGenerationSettings().isNeedsEnabling());
        serializer.writeBoolean(this.getGenerationSettings().isNeedsBuilding());
        serializer.writeObject(meta);
        serializer.writeSerializer(serializer1);

        //NOTE: Any new fields that need to be serialized should be serialized using the meta object and the path "internal."
    }

    /**
     * Get the size of this element
     *
     * @return Size of this element as a 2D int vector
     */
    public abstract IntVector2D getShape();

    /**
     * Get the schematic to be built for this element
     *
     * @return Schematic
     */
    @NotNull
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow(
                getName().replaceAll(" ", "-").toLowerCase() +
                        "-" + level + (destroyed ? "-destroyed" : ""));
    }

    /**
     * Get the schematic to be built for this element
     *
     * @return the schematic to be built
     */
    public final Schematic getSchematic() {
        return getSchematic(this.level, this.destroyed);
    }

    /**
     * Whether this element is visible to any attackers
     */
    public boolean isVisibleToAttackers() {
        return true;
    }

    /**
     * Called when this element is built
     */
    protected void onBuild(AsyncWorld world) {
    }

    /**
     * Called when this element is removed
     */
    protected void onRemove() {
    }

    /**
     * Called when any functions this element performs should start being executed - Can be called async
     */
    protected void onEnable() {

    }

    /**
     * Called when the element is added to the bunker
     */
    protected void onPlacement() {

    }

    /**
     * Called when any neighbouring element calls updateNeighbours
     */
    protected void onUpdate() {
    }

    /**
     * Called when any functions this element performs should cease to execute - Can be called async
     */
    protected void onDisable() {

    }

    /**
     * Called when this element is damaged
     *
     * @param amount the amount of damage that was done
     */
    protected void onDamage(double amount) {

    }

    /**
     * Called when this element's child tiles are replaced with their corresponding sub schematics of the bunker map schematic
     */
    protected void onUnBuild(AsyncWorld world) {

    }

    /**
     * Called when this element's leve is set
     *
     * @param level the new level
     */
    protected void onLevelSet(int level) {

    }

    /**
     * Override this method to save stuff to this element's meta before saving and serialization
     * a serializer is provided if you want to serialize your own stuff manually
     */
    protected void onSaveAsync(GravSerializer serializer) {
    }

    /**
     * Called when this element is right clicked by the bunker tool
     * Default implementation opens an occupied tile menu
     * Overriding will replace that implementation
     * call super.onClick(e) to open that occupied tile menu
     *
     * @param e Interact event
     */
    public void onClickTool(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            e.setCancelled(true);
            new MenuTileOccupied(getTile()).open(e.getPlayer());
        }
    }

    /**
     * Called when this element is clicked by a player
     *
     * @param e
     */
    public void onClick(PlayerInteractEvent e) {

    }

    /**
     * Called when this element is ticked and is enabled
     */
    public void onTick() {
    }

    /**
     * Called when this element is ticked, will be called regardless of whether this element is enabled or not
     */
    public void onTickEnabledOrDisabled() {
    }

    /**
     * Get the maximum health of this element
     *
     * @return Maximum health
     */
    public abstract double getMaxHealth();

    /**
     * Get the name of this element
     *
     * @return The name of this element
     */
    public String getName() {
        return getType().getName();
    }

    /**
     * Set the level of this element
     *
     * @param level the new level
     */
    public void setLevel(int level) {
        this.level = level;
        this.onLevelSet(level);
    }

    /**
     * Get the bounding box of this element
     *
     * @param height Height of the element
     * @return Bounding box of this element
     */
    public CuboidRegion getBoundingRegion(double height) {
        return new CuboidRegion(this.getLocation(), this.getLocation().add(this.getShape().toLocation(this.getBunker().getWorld().getBukkitWorld(), 0).multiply(BunkerManager.TILE_SIZE_BLOCKS).subtract(0.00000001, 0, 0.00000001).add(0, height, 0)));
    }

    /**
     * Starts execution of any functions this element should perform
     */
    public final synchronized void enable() {
        if (enabled)
            return;
        this.enabled = true;
        this.setGenerationSettings(new ElementGenerationSettings(true, getGenerationSettings().isNeedsBuilding()));
        this.onEnable();
    }

    /**
     * Stops execution of any functions this element should perform
     *
     * @param persistent Whether or not this element will still be disabled after a reload of the bunker
     */
    public final synchronized void disable(boolean persistent) {
        if (!enabled)
            return;
        this.enabled = false;
        if (persistent)
            this.setGenerationSettings(new ElementGenerationSettings(false, getGenerationSettings().isNeedsBuilding()));
        this.onDisable();
    }

    public final synchronized void disable() {
        this.disable(false);
    }

    /**
     * Gets the information about this element
     *
     * @return the information about this element
     */
    public MenuElement getInfoElement() {
        String name = getName();
        String description = getDescription();
        String health = "&7" + MathUtils.round(getHealth(), 1) + "&f/&7" + getMaxHealth();
        String level = "&7" + getLevel() + "&f/&7" + getMaxLevel();
        String dimensions = getShape().getX() + "x" + getShape().getY();
        return new MenuElement(new ItemBuilder(Material.PAPER, 1).setName("&f&l" + name)
                .addLore("&7" + description)
                .addLore("")
                .addLore("&fHealth: " + health)
                .addLore("&fLevel: " + level)
                .addLore("&fDims: &7" + dimensions)
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
    }

    /**
     * Get the description of this element
     *
     * @return the description of this element
     */
    public String getDescription() {
        return this.getType().getDescription();
    }

    /**
     * Set the height of this element
     *
     * @param health the health of this element to set to
     */
    public final void setHealth(double health) {
        if (health > getMaxHealth())
            health = getMaxHealth();
        this.health = health;
    }

    /**
     * Called when this element is reaches 0 health (DIFFERENT FROM onRemove)
     *
     * @return Whether or not to destroy the element
     */
    public boolean onDestroy() {
        return true;
    }

    public abstract BunkerElementType getType();

    /**
     * Build this element and if specified, update it's neighbors
     *
     * @param updateNeighbours Whether or not to update the neighbors
     */
    public synchronized void build(boolean updateNeighbours) {
        SpigotAsyncWorld world = new SpigotAsyncWorld(bunker.getWorld().getBukkitWorld());
        build(world, updateNeighbours);
        world.flush();
    }

    /**
     * Build this element and update its neighbors
     */
    public synchronized void build() {
        build(true);
    }

    /**
     * Must call flush on the world afterwards
     *
     * @param world the world
     */
    public synchronized void build(AsyncWorld world) {
        build(world, true);
    }

    /**
     * Must call flush on the world afterwards
     *
     * @param world            the world to build into
     * @param updateNeighbours whether or not to update neighbours
     */
    public synchronized void build(AsyncWorld world, boolean updateNeighbours) {
        //If already built
        if (built) {
            this.unBuild(world, false);
        }
        Schematic schematic = null;
        try {
            //Try to get schematic
            schematic = getSchematic().rotate(this.getRotation());
            world.pasteSchematic(schematic, this.bunker.getTileMap().getTileLocation(this.getPosition()));
        } catch (RuntimeException e) {
            //Use default schematic instead
            e.printStackTrace();
            try {
                schematic = BunkerSchematics.getDefaultSchematic(this.getShape());
                for (int i = 0; i < getShape().getX(); i++) {
                    for (int j = 0; j < getShape().getY(); j++) {
                        IntVector2D pos = this.getPosition().add(new IntVector2D(i, j));
                        world.pasteSchematic(schematic, this.bunker.getTileMap().getTileLocation(pos));
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setGenerationSettings(new ElementGenerationSettings(getGenerationSettings().isNeedsEnabling(), true));
        this.built = true;
        try {
            this.onBuild(world);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION in onBuild of " + this.getClass().getSimpleName());
        }
        this.damageAnimationHandler.cleanup();
        this.damageAnimationHandler = new DamageAnimationHandler(this, schematic == null ? 10 : schematic.getDimensions().getY() + (1.65D * (schematic.getDimensions().getX() / 10D + schematic.getDimensions().getZ() / 10D) / 2D) - schematic.getOrigin().getY(), 120);

        if (updateNeighbours)
            updateNeighbours();
    }

    /**
     * Replaces this element with it's corresponding sub schematic of the bunker map schematic (un-builds it basically)
     * Must call flush afterwards
     *
     * @param world            the world
     * @param updateNeighbours whether to update neighbours
     */
    public void unBuild(AsyncWorld world, boolean updateNeighbours) {
        Schematic mapSchematic = this.bunker.getMapSchematic();
        IntVector2D tileMin = this.bunker.getTileMap().getTileLocation2D(this.getPosition());
        IntVector2D tileMax = tileMin.add(this.getShape().multiply(BunkerManager.TILE_SIZE_BLOCKS)).subtract(new IntVector2D(1, 1));
        Schematic tileSchem = mapSchematic.subSchemXZ(tileMin, tileMax);
        world.pasteSchematic(tileSchem, tileMin.toIntVector3D(this.bunker.getBunkerLocation().getY()));
        onUnBuild(world);
        if (updateNeighbours)
            updateNeighbours();
    }

    /**
     * Update this element
     */
    public final void update() {
        onUpdate();
    }

    /**
     * Call update on this element's neighbours
     */
    public final void updateNeighbours() {
        BunkerElement[] neighbours = getNeighbours();
        for (BunkerElement element : neighbours) {
            if (element != null) {
                element.update();
            }
        }
    }

    protected final synchronized boolean is_beingRemoved() {
        return _beingRemoved;
    }

    /**
     * Remove this element
     */
    public synchronized void remove() {
        remove(true);
    }

    /**
     * Rebuilds schematic as destroyed
     * returns whether the element is now destroyed
     */
    public synchronized boolean destroy() {
        if (this.destroyed)
            return true;
        if (!this.onDestroy()) {
            return false;
        }
        this.destroyed = true;
        this.build();
        this.disable(true);
        return true;
    }

    /**
     * This basically just sets destroyed to false, resets health, and rebuilds
     */
    public synchronized void unDestroy() {
        if (!this.isDestroyed()) {
            return;
        }
        this.destroyed = false;
        this.setHealth(this.getMaxHealth());
        this.build();
        this.enable();
    }

    /**
     * Set whether this element is destroyed or not
     */
    public synchronized void setDestroyed(boolean value) {
        this.destroyed = value;
    }

    /**
     * Removes this element from the bunker
     */
    public synchronized void remove(boolean updateNeighbours) {
        if (this.getBunker() == null || !bunker.getTileMap().getElements().contains(this) || _beingRemoved)
            return;
        _beingRemoved = true;
        try {
            this.disable();
            this.getDamageAnimationHandler().cleanup();
            this.onRemove();
            this.getBunker().removeElement(this.getPosition().getX(), this.getPosition().getY());
            AsyncWorld world = new SpigotAsyncWorld(this.getBunker().getWorld().getBukkitWorld());
            this.unBuild(world, false);
            world.flush();
            this.built = false;

            if (updateNeighbours)
                this.updateNeighbours();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("PROBLEM WHILE REMOVING ELEMENT OF TYPE " + this.getClass().getSimpleName());
        } finally {
            _beingRemoved = false;
        }
    }

    /**
     * Get the maximum level of this element
     *
     * @return the maximum level of this element
     */
    public int getMaxLevel() {
        return DEFAULT_MAX_LEVEL;
    }

    /**
     * Should this element be allowed to be destroyed by attackers, etc.
     * Default value: true
     */
    public boolean isRemovable() {
        return true;
    }

    public Tile getTile() {
        return getBunker().getTileMap().getTile(getPosition());
    }

    /**
     * tick this element
     */
    public final void tick() {
        this.getDamageAnimationHandler().tick();
        try {
            this.onTickEnabledOrDisabled();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (this.isEnabled())
                this.onTick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get the drops for an amount of hypothetical damage to this element
     *
     * @param damage Amount of damage
     * @return Element drop that would be dropped due to the specified damage
     */
    public abstract ElementDrop getDropForDamage(double damage);

    /**
     * Do damage to this element
     *
     * @param amount The amount of damage
     */
    public ElementDrop damage(double amount) {
        ElementDrop drop = getDropForDamage(amount);

        BunkerElementDamageEvent event = new BunkerElementDamageEvent(this, amount, drop);
        Bukkit.getPluginManager().callEvent(event);

        amount = event.getDamage();
        drop = event.getDrop();

        double health = getHealth();
        health -= amount;
        if (health < 0D)
            health = 0D;
        this.getDamageAnimationHandler().animate(amount);
        this.setHealth(health);
        this.onDamage(amount);
        if (health <= 0D)
            this.destroy();
        return drop;
    }

    /**
     * Get the Bukkit location of this element
     *
     * @return The bukkit location of this element
     */
    public final Location getLocation() {
        return bunker.getBunkerLocation()
                .addXZ(getPosition().multiply(BunkerManager.TILE_SIZE_BLOCKS))
                .toBukkitVector()
                .toLocation(bunker.getWorld().getBukkitWorld());
    }

    /**
     * Get the element on the specified relative tile
     *
     * @param x Relative tile x
     * @param z Relative tile z
     * @return The element on the specified relative tile
     */
    public final BunkerElement getRelative(int x, int z) {
        try {
            Tile tile = getBunker().getTileMap()
                    .getTile(getPosition().getX() + x, getPosition().getY() + z);
            if (tile == null) {
                return null;
            }
            return tile.getParent();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Get all the neighbours of this element's origin tile
     *
     * @return all the neighbours of this element's origin tile
     */
    public final BunkerElement[] getNeighbours() {
        return new BunkerElement[]{getRelative(1, 0), getRelative(0, 1), getRelative(-1, 0), getRelative(0, -1)};
    }

    /**
     * Get all flags held by this element
     */
    public List<BunkerElementFlag> getFlags() {
        flagListLock.lock();
        try {
            validateFlags();
            return meta.get("internal.flags");
        } finally {
            flagListLock.unlock();
        }
    }

    /**
     * I AM JAVADOC
     */
    private void validateFlags() {
        flagListLock.lock();
        try {
            List<BunkerElementFlag> flags = meta.get("internal.flags");
            if (flags == null) {
                List<BunkerElementFlag> defaultFlags = new ArrayList<>();
                defaultFlags.add(BunkerElementFlag.PROTECTED);
                meta.set("internal.flags", defaultFlags);
            }
        } finally {
            flagListLock.unlock();
        }
    }

    /**
     * Check if this element holds a flag
     */
    public boolean hasFlag(BunkerElementFlag flag) {
        return getFlags().contains(flag);
    }

    /**
     * Add a flag to this element
     */
    public void addFlag(BunkerElementFlag flag) {
        flagListLock.lock();
        try {
            validateFlags();
            List<BunkerElementFlag> flags = meta.get("internal.flags");
            if (!flags.contains(flag)) {
                flags.add(flag);
            }
        } finally {
            flagListLock.unlock();
        }
    }

    /**
     * Remove a flag from this element
     */
    public void removeFlag(BunkerElementFlag flag) {
        flagListLock.lock();
        try {
            validateFlags();
            meta.<List<BunkerElementFlag>>get("internal.flags").remove(flag);
        } finally {
            flagListLock.unlock();
        }
    }
}