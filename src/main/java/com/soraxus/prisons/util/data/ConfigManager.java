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

    public ConfigManager(Plugin parent, String folder) {
        this.parent = parent;
        this.folder = folder;
        init();
    }

    private HashMap<String, FileConfiguration> configs = new HashMap<>();

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

    public boolean configExists(String config) {
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        File f = new File(configFolder, config);
        return f.exists();
    }

    public void deleteConfig(String config) {
        configs.remove(config);
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        File f = new File(configFolder, config);
        if (f.exists()) {
            f.delete();
        }
    }

    public FileConfiguration getConfig(String config) {
        if (!configs.containsKey(config)) {
            File dataFolder = parent.getDataFolder();
            File configFolder = new File(dataFolder, folder);
            File f = new File(configFolder, config);
            if (f.exists()) {
                configs.put(config, YamlConfiguration.loadConfiguration(f));
            } else {
                configs.put(config, new YamlConfiguration());
            }
            saveConfig(config);
        }
        return configs.get(config);
    }

    public void setConfig(FileConfiguration config, String cfg) {
        configs.put(cfg, config);
        saveConfig(cfg);
    }

    public void saveConfig(String cfg) {
        FileConfiguration config = configs.get(cfg);
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        configFolder.mkdirs();
        File configFile = new File(configFolder, cfg + ".yml");
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            config.save(configFile);
        } catch (IOException e) {
            System.out.println("Error while saving the config: " + cfg);
        }
    }

    public void reloadConfig(String cfg) {
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        configFolder.mkdirs();
        File configFile = new File(configFolder, cfg + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configs.put(cfg, config);
    }

    public void deInit() {
        File dataFolder = parent.getDataFolder();
        File configFolder = new File(dataFolder, folder);
        configFolder.mkdirs();
        for (String str : configs.keySet()) {
            File configFile = new File(configFolder, str + ".yml");
            try {
                configs.get(str).save(configFile);
            } catch (IOException e) {
                System.out.println("Error while saving configs: " + e.getMessage());
            }
        }
    }
}