package com.soraxus.prisons.bunkers.base.elements.defense.nonactive;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.ConnectingElement;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallParameter;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallType;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A mine that blows up your enemies
 */
public class ElementWall extends ConnectingElement {
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
    public ElementWall(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    /**
     * Create a new mine
     *
     * @param bunker Bunker to make the mine in
     */
    public ElementWall(Bunker bunker) {
        super(null, bunker);
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

    @Override
    public void onTick() {

    }

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
     * Get the schematic of this mine
     *
     * @return Schematic
     */
    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        BunkerElement[] neighbours = getNeighbours();
        int[] bls = Arrays.stream(neighbours)
                .mapToInt(n -> this.getClass().isInstance(n) ? 1 : 0)
                .toArray();
        WallParameter param = WallType.getWall(bls);
        this.setRotation(param.getRotation());
        return BunkerSchematics.getWithoutThrow(param.getType().getSchematicName(level) + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public void onUpdate() {
        this.build(false);
    }

    /**
     * Get the maximum health of this wall
     *
     * @return 1
     */
    @Override
    public double getMaxHealth() {
        return 100 * Math.pow(getLevel(), 1.5);
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.DEFENSIVE_WALL;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

}
