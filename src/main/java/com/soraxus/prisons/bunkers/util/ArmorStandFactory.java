package com.soraxus.prisons.bunkers.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class ArmorStandFactory {
    /**
     * Create a hidden armor stand at
     *
     * @param l Location
     * @return ArmorStand
     */
    public static ArmorStand createHidden(Location l) {
        if (!l.getChunk().isLoaded()) {
            l.getChunk().load();
        }
        ArmorStand stand = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setCanPickupItems(false);
        stand.setGravity(false);
        return stand;
    }

    /**
     * Create an invisible armor stand
     * with a name
     *
     * @param l    Location
     * @param text Name
     * @return ArmorStand
     */
    public static ArmorStand createText(Location l, String text) {
        ArmorStand stand = ArmorStandFactory.createHidden(l);
        stand.setVisible(false);
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);
        return stand;
    }

    /**
     * Create a big invisible armor stand
     * with a name
     *
     * @param l    Location
     * @param text Name
     * @return ArmorStand
     */
    public static ArmorStand createTextAdultSize(Location l, String text) {
        ArmorStand stand = ArmorStandFactory.createHidden(l);
        stand.setSmall(false);
        stand.setVisible(false);
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);
        return stand;
    }
}
