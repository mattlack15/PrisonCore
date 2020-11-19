/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ConfigManager {
    private Plugin parent;
    private String folder;
    private HashMap<String, FileConfiguration> configs = new HashMap<>();

    /**
     * Create a new ConfigManager in a certain folder
     *
     * @param parent Parent plugin
     * @param folder Folder path (within the plugin's data folder)
     */
    public ConfigManager(Plugin parent, String folder) {
        this.parent = parent;
        this.folder = folder;
        init();
    }

    /**
     * Initialize required files and load existing configs
     */
    void init() {
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        configFolder.mkdir();
        File[] files = configFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(f);
            try {
                config.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            configs.put(f.getName().replace(".yml", ""), config);
        }
    }

    /**
     * Get the file for a specific config
     *
     * @param config Config name
     * @return File
     */
    private File getFile(String config) {
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        return new File(configFolder, config + ".yml");
    }

    /**
     * Check if a specific config exists
     *
     * @param config Config name
     * @return <code>true</code> if the config exists
     */
    public boolean configExists(String config) {
        return getFile(config).exists();
    }

    /**
     * Delete a specific config
     *
     * @param config Config name
     */
    public void deleteConfig(String config) {
        configs.remove(config);
        File f = getFile(config);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * Get a config by its name
     * Note: Creates the config if it does not exist
     *
     * @param config Config name
     * @return FileConfiguration
     */
    public FileConfiguration getConfig(String config) {
        if (!configs.containsKey(config)) {
            File f = getFile(config);
            if (f.exists()) {
                configs.put(config, YamlConfiguration.loadConfiguration(f));
            } else {
                configs.put(config, new YamlConfiguration());
            }
            saveConfig(config);
        }
        return configs.get(config);
    }

    /**
     * Set a certain config, this can be used to completely override
     * a configuration file.
     *
     * @param config FileConfiguration
     * @param cfg    Config name
     */
    public void setConfig(FileConfiguration config, String cfg) {
        configs.put(cfg, config);
        saveConfig(cfg);
    }

    /**
     * Save a config to a file
     *
     * @param cfg Config name
     */
    public void saveConfig(String cfg) {
        FileConfiguration config = configs.get(cfg);
        File configFile = getFile(cfg);
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            config.save(configFile);
        } catch (IOException e) {
            System.out.println("Error while saving the config: " + cfg);
        }
    }

    /**
     * Reload a specific config from disk
     *
     * @param cfg Config name
     */
    public void reloadConfig(String cfg) {
        File configFile = getFile(cfg);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configs.put(cfg, config);
    }

    /**
     * De-initialize this ConfigManager
     * Saves all configs to disk
     * This should always be called before server shutdown
     */
    public void deInit() {
        for (String str : configs.keySet()) {
            File configFile = getFile(str);
            try {
                configs.get(str).save(configFile);
            } catch (IOException e) {
                System.out.println("Error while saving configs: " + e.getMessage());
            }
        }
    }
}