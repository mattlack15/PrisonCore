package com.soraxus.prisons.util;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class WorldUtil {
    public static Entity getEntity(World world, UUID id) {
        for (Entity ent : world.getEntities()) {
            if (ent.getUniqueId().equals(id)) {
                return ent;
            }
        }
        return null;
    }
}
