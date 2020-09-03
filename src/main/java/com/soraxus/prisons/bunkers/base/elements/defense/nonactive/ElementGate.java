package com.soraxus.prisons.bunkers.base.elements.defense.nonactive;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallParameter;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall.WallType;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.tools.ToolUtils;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ElementGate extends ElementWall {
    public ElementGate(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementGate(Bunker bunker) {
        super(bunker);
    }

    public synchronized boolean isOpen() {
        return getMeta().getOrSet("open", false);
    }

    public synchronized void setOpen(boolean open) {
        boolean rebuild = isOpen() != open;
        getMeta().set("open", open);
        getBunker().getWorld().getBukkitWorld().playSound(
                getLocation().add(3, 3, 3),
                open ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE,
                1F, 0.8f);
        if(rebuild)
            this.build();
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        BunkerElement[] neighbours = getNeighbours();
        int[] bls = Arrays.stream(neighbours)
                .mapToInt(n -> n instanceof ElementWall ? 1 : 0)
                .toArray();
        WallParameter param = WallType.getWall(bls);
        this.setRotation(param.getRotation());
        return BunkerSchematics.get("gate-" + level + "-" + (isOpen() ? "open" : "closed") + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.DEFENSIVE_GATE;
    }

    @Override
    public void onClick(PlayerInteractEvent e) {
        if(e.getItem() != null && ToolUtils.isDefaultTool(e.getItem()))
            return;
        this.setOpen(!this.isOpen());
    }
}
