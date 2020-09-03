/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.enchants.manager;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownUtils {
    private static Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    private static Map<String, Long> getCooldowns(UUID str) {
        if (!cooldowns.containsKey(str)) {
            return new HashMap<>();
        }
        return cooldowns.get(str);
    }

    private static void setCooldowns(UUID str, Map<String, Long> cds) {
        cooldowns.put(str, cds);
    }

    /**
     * Sets a cooldown for the player
     *
     * @param player   Player to set the cooldown for
     * @param str      Name of the cooldown
     * @param cooldown Cooldown time in seconds
     */
    public static void setCooldown(@NotNull HumanEntity player, String str, int cooldown) {
        long timestamp = System.currentTimeMillis() + 1000 * cooldown;
        Map<String, Long> ts = getCooldowns(player.getUniqueId());
        ts.put(str, timestamp);
        setCooldowns(player.getUniqueId(), ts);
    }

    /**
     * Checks whether a player's cooldown has expired
     *
     * @param player Player to check
     * @param str    Name of the cooldown
     * @return Whether cooldown has expired
     */
    public static boolean isCooldown(@NotNull HumanEntity player, String str) {
        Map<String, Long> ts = getCooldowns(player.getUniqueId());
        if (!ts.containsKey(str))
            return true;
        return (ts.get(str) < System.currentTimeMillis());
    }

    public static long getCooldownTime(@NotNull HumanEntity player, String str) {
        Map<String, Long> ts = getCooldowns(player.getUniqueId());
        if (!ts.containsKey(str))
            return 0;
        return ts.get(str) - System.currentTimeMillis();
    }
}
