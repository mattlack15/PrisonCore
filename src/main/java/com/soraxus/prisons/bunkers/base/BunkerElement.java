package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.bunkers.base.elements.animation.DamageAnimationHandler;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.gui.tile.MenuTileOccupied;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.ItemBuilder;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public abstract class BunkerElement implements GravSerializable {
    public static final int DEFAULT_MAX_LEVEL = 10;

    public static final Random random = ThreadLocalRandom.current();
    private final Bunker bunker;
    private Meta meta = new Meta();
    private UUID id = UUID.randomUUID();
    @Setter(AccessLevel.PROTECTED)
    private IntVector2D position;
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
        this.bunker = bunker;
        if (serializer == null)
            return;

        //Deserialize
        id = serializer.readUUID();
        position = serializer.readObject();
        level = serializer.readInt();
        health = serializer.readDouble();
        destroyed = serializer.readBoolean();
        this.setGenerationSettings(new ElementGenerationSettings(serializer.readBoolean(), serializer.readBoolean()));
        meta = serializer.readObject();
        GravSerializer serializer1 = serializer.readSerializer();
        try {
            this.onLoadAsync(serializer1);
        } catch (Exception e) {
            e.printStackTrace();
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

    ;

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
        return BunkerSchematics.get(
                getName().replaceAll(" ", "-").toLowerCase() +
                        "-" + level + (destroyed ? "-destroyed" : ""));
    }

    /**
     * Get the schematic to be built for this element
     *
     * @return the schematic to be built
     */
    public Schematic getSchematic() {
        return getSchematic(this.level, this.destroyed);
    }

    /**
     * Whether this element is visible to any attackers
     */
    public boolean isVisibleToAttackers() {
        return true;
    }

    /**
     * Called when it is time to create entities
     */
    public void createEntities() {
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
     * called when this element is ticked
     */
    public void onTick() {
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
        return new CuboidRegion(this.getLocation(), this.getLocation().add(this.getShape().toLocation(this.getBunker().getWorld().getBukkitWorld(), 0).multiply(BunkerManager.TILE_SIZE_BLOCKS).subtract(1, 0, 1).add(0, height, 0)));
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
     */
    public final synchronized void disable() {
        if (!enabled)
            return;
        this.enabled = false;
        this.onDisable();
    }

    /**
     * Gets the information about this element
     *
     * @return the information about this element
     */
    public MenuElement getInfoElement() {
        String name = getName();
        String description = getDescription();
        String health = "&7" + getHealth() + "&f/&7" + getMaxHealth();
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
     * Return whether or not to destroy the element
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
            schematic = getSchematic().rotate(this.getRotation());
            world.pasteSchematic(schematic, this.bunker.getTileMap().getTileLocation(this.getPosition()));
        } catch (RuntimeException e) {
            try {
                schematic = BunkerManager.instance.getDefaultSchematic();
                for (int i = 0; i < getShape().getX(); i ++) {
                    for (int j = 0; j < getShape().getY(); j ++) {
                        IntVector2D pos = this.getPosition().add(new IntVector2D(i, j));
                        world.pasteSchematic(schematic, this.bunker.getTileMap().getTileLocation(pos));
                    }
                }
            } catch(Exception e1) {
                e1.printStackTrace();
            }
        } catch(Exception e) {
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
        return true;
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
    public void damage(double amount) {
        double health = getHealth();
        health -= amount;
        if (health < 0D)
            health = 0D;
        this.getDamageAnimationHandler().animate(amount);
        this.setHealth(health);
        this.onDamage(amount);
        if (health <= 0D)
            this.destroy();
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
}