package com.soraxus.prisons.bunkers.util;

import com.soraxus.prisons.util.WorldUtil;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class BHoloTextBox implements GravSerializable {
    private static final double AS_HEIGHT = 0.25;

    private Location location;
    private double lineSpacing;
    private boolean moveUpwards;

    private List<UUID> lines = new ArrayList<>();

    private Supplier<World> worldSupplier;

    public BHoloTextBox(Location location, double lineSpacing, boolean moveUpwards, Supplier<World> worldSupplier) {
        this.location = location;
        this.lineSpacing = lineSpacing;
        this.moveUpwards = moveUpwards;
        this.worldSupplier = worldSupplier;
    }

    public BHoloTextBox(GravSerializer serializer) {
        this.location = serializer.readObject();
        this.lineSpacing = serializer.readDouble();
        this.moveUpwards = serializer.readBoolean();
        this.lines = serializer.readObject();
    }

    private ArmorStand getEntity(int line) {
        UUID id = this.lines.get(line);
        return (ArmorStand) WorldUtil.getEntity(worldSupplier.get(), id);
    }

    /**
     * Set the text of a line
     */
    public void setLine(int line, String text) {
        if (this.lines.size() <= line) {
            return;
        }
        if (isChunkUnloaded())
            return;

        text = ChatColor.translateAlternateColorCodes('&', text);

        ArmorStand stand = this.getEntity(line);
        if (stand == null) {
            this.lines.set(line, ArmorStandFactory.createText(this.getLocationOfLine(line), text).getUniqueId());
            stand = getEntity(line);
        }
        stand.setVisible(false);
        stand.setCustomName(text);
    }

    public boolean isChunkUnloaded() {
        return !location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * Removes a line
     */
    public void removeLine(int line) {
        if (this.lines.size() <= line) {
            return;
        }
        ArmorStand stand = getEntity(line);
        if (stand != null) stand.remove();
        this.lines.remove(line);
    }

    /**
     * Get the text of all lines
     */
    public ArrayList<String> getLines() {
        ArrayList<String> out = new ArrayList<>();
        for (int line = 0; line < this.lines.size(); line++) {
            ArmorStand stand = getEntity(line);
            if (isChunkUnloaded()) {
                out.add("");
            }
            if (stand != null) {
                stand.setVisible(false);
                out.add(stand.getCustomName());
            }
        }
        return out;
    }

    /**
     * Adds a line
     */
    public void addLine(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        ArmorStand stand = ArmorStandFactory.createText(this.getLocationOfLine(this.lines.size()), text);
        stand.setVisible(false);
        this.lines.add(stand.getUniqueId());
    }

    public void setOrMake(int line, String text) {
        if (isChunkUnloaded())
            return;
        while (this.getLines().size() <= line) {
            addLine(text);
        }
        setLine(line, text);
    }

    /**
     * Gets the location of the text on a certain line
     * Lines start at 0
     */
    public Location getLocationOfLine(int line) {
        Location baseLoc = location.clone().subtract(new Vector(0, AS_HEIGHT, 0));
        return baseLoc.add(new Vector(0, lineSpacing * line * (moveUpwards ? 1 : -1), 0));
    }

    /**
     * Removes all lines
     */
    public void clear() {
        location.getChunk().load();
        for (UUID line : this.lines) {
            ArmorStand stand = (ArmorStand) WorldUtil.getEntity(worldSupplier.get(), line);
            if (stand != null)
                stand.remove();
        }
        this.lines.clear();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(location);
        serializer.writeDouble(lineSpacing);
        serializer.writeBoolean(moveUpwards);
        serializer.writeObject(lines);
    }
}
