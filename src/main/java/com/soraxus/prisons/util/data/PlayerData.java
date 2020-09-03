package com.soraxus.prisons.util.data;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private static HashMap<UUID, FileConfiguration> loadedPlayers = new HashMap<>();

    public static void init() {
        Scheduler.scheduleSyncRepeatingTask(PlayerData::saveAll, 100, 60);
        Scheduler.scheduleSyncRepeatingTask(PlayerData::update, 100, 180);
    }

    public static void deinit() {
        PlayerData.saveAll();
    }

    public static void saveAll() {
        loadedPlayers.forEach((key, value) -> savePlayerData(key));
    }

    public static FileConfiguration getPlayerData(OfflinePlayer op) {
        return getPlayerData(op.getUniqueId());
    }

    /**
     * Get the current loaded playerdata for this player
     * and load it if there is none
     *
     * @param id Player UUID
     * @return Player data
     */
    public static FileConfiguration getPlayerData(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }
        if (loadedPlayers.containsKey(id)) {
            return loadedPlayers.get(id);
        }
        File folder = new File(SpigotPrisonCore.instance.getDataFolder(), "players");
        File file = new File(folder, id + ".yml");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    System.out.println("Non-existent file could not be created.");
                }
            } catch (IOException e) {
                System.out.println("Error while attempting to create player data file for " + id);
                e.printStackTrace();
            }
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        loadedPlayers.put(id, cfg);
        return cfg;
    }

    public static void update() {
        new ArrayList<>(loadedPlayers.keySet()).stream()
                .filter(i -> Bukkit.getPlayer(i) == null)
                .forEach(PlayerData::unloadPlayerData);
    }

    public static List<UUID> getDatas() {
        List<UUID> ids = new ArrayList<>();
        File folder = new File(SpigotPrisonCore.instance.getDataFolder(), "players");
        String[] fs = folder.list();
        if (!folder.isDirectory() || fs == null) {
            return ids;
        }
        for (String s : fs) {
            ids.add(UUID.fromString(s.replaceAll("\\.yml", "")));
        }
        return ids;
    }

    /**
     * Save the data of the player
     *
     * @param op Player
     */
    public static void savePlayerData(OfflinePlayer op) {
        savePlayerData(op.getUniqueId());
    }

    /**
     * Save the data of the player
     *
     * @param id UUID
     */
    public static void savePlayerData(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }
        if (!loadedPlayers.containsKey(id) || loadedPlayers.get(id) == null) {
            loadedPlayers.remove(id);
            return;
        }
        File folder = new File(SpigotPrisonCore.instance.getDataFolder(), "players");
        File file = new File(folder, id + ".yml");
        try {
            loadedPlayers.get(id).save(file);
        } catch (IOException e) {
            System.out.println("Error while attempting to save player data file for " + id);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Config Data: " + loadedPlayers.get(id).saveToString());
            e.printStackTrace();
        }
    }

    public static void unloadPlayerData(OfflinePlayer op) {
        unloadPlayerData(op.getUniqueId());
    }

    /**
     * Unload the data of the player
     *
     * @param id UUID of Player
     */
    public static void unloadPlayerData(UUID id) {
        if (id == null) {
            return;
        }
        savePlayerData(id);
        loadedPlayers.remove(id);
    }

    /**
     * Reload the data of the player
     *
     * @param op Player
     */
    public static void reloadPlayerData(OfflinePlayer op) {
        unloadPlayerData(op);
        getPlayerData(op);
    }

    public static String getString(UUID id, String path) {
        return getPlayerData(id).getString(path);
    }
    public static int getInt(UUID id, String path) {
        return getPlayerData(id).getInt(path);
    }
    public static long getLong(UUID id, String path) {
        return getPlayerData(id).getLong(path);
    }

    public static <T> T set(UUID id, String path, T value) {
        getPlayerData(id).set(path, value);
        return value;
    }
}
