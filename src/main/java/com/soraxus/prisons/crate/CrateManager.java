package com.soraxus.prisons.crate;

import com.soraxus.prisons.core.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class CrateManager extends Manager<Crate, String> {
    public static CrateManager instance;

    private List<Crate> crates = new ArrayList<>();

    private FileConfiguration config;
    private File configFile;

    public CrateManager(File configFile) {
        instance = this;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.configFile = configFile;
    }

    public synchronized void loadAll() {
        for (String keys : config.getKeys(false)) {
            ConfigurationSection crateSection = config.getConfigurationSection(keys);
            Crate crate = Crate.fromSection(crateSection);
            this.crates.add(crate);
        }
    }

    @Override
    public synchronized List<Crate> getLoaded() {
        return new ArrayList<>(this.crates);
    }

    @Override
    public synchronized boolean add(Crate object) {
        if (this.get(object.getName()) == null) {
            this.crates.add(object);
            this.saveCrate(object);
            return true;
        }
        return false;
    }

    @Override
    public synchronized Crate get(String identifier) {
        for (Crate crate : crates) {
            if (crate.getName().equals(identifier))
                return crate;
        }
        return null;
    }

    @Override
    public synchronized Future<Crate> load(String identifier) {
        return null;
    }

    @Override
    public synchronized Future<Void> unload(String identifier) {
        this.crates.removeIf(c -> c.getName().equals(identifier));
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }

    public synchronized void saveCrate(Crate crate) {
        crate.saveTo(config.createSection(crate.getName()));
        try {
            this.config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Future<Void> remove(String identifier) {
        config.set(identifier, null);
        this.unload(identifier);
        try {
            this.config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }
}
