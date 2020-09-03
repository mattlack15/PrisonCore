package com.soraxus.prisons.mines.manager;

import com.soraxus.prisons.core.Manager;
import com.soraxus.prisons.mines.MineFiles;
import com.soraxus.prisons.mines.object.Mine;
import lombok.Getter;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MineManager extends Manager<Mine, String> {

    public static MineManager instance;

    private ExecutorService service = Executors.newCachedThreadPool();

    private List<Mine> loadedMines = new ArrayList<>();

    @Getter
    private FileConfiguration minesConfig = YamlConfiguration.loadConfiguration(MineFiles.MINES_FILE);

    private ConcurrentMap<String, Future<Void>> saveOperations = new ConcurrentHashMap<>();

    public MineManager() {
        instance = this;
    }

    /**
     * Waits for all save operations
     */
    public synchronized void joinSaveOps() {
        for (Map.Entry<String, Future<Void>> entry : new HashMap<>(saveOperations).entrySet()) {
            Future<Void> v = entry.getValue();
            try {
                v.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        saveOperations.clear();
    }

    @Override
    public synchronized List<Mine> getLoaded() {
        return new ArrayList<>(loadedMines);
    }

    @Override
    public synchronized boolean add(Mine object) {
        if (get(object.getName()) != null)
            return false;
        loadedMines.add(object);
        queueSaveMineOperation(object);
        return true;
    }

    @Override
    public synchronized Mine get(String identifier) {
        for (Mine mines : loadedMines) {
            if (mines.getName().equalsIgnoreCase(identifier))
                return mines;
        }
        return null;
    }

    public synchronized Mine getMineOf(Location location) {
        for(Mine mines : loadedMines) {
            if(mines.getRegion().getWorld().equals(location.getWorld()) && mines.getRegion().contains(Vector3D.fromBukkitVector(location.toVector()))) {
                return mines;
            }
        }
        return null;
    }

    @Override
    public synchronized Future<Mine> load(String identifier) {
        CompletableFuture<Mine> future = new CompletableFuture<>();
        if (!minesConfig.isConfigurationSection(identifier)) {
            future.complete(null);
            return future;
        }

        ConfigurationSection section = minesConfig.getConfigurationSection(identifier);

        Mine mine = Mine.fromConfigSection(section);
        this.loadedMines.add(mine);
        future.complete(mine);
        return future;
    }

    public synchronized Future<Void> loadAll() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        for(String keys : minesConfig.getKeys(false)) {
            Mine mine = Mine.fromConfigSection(minesConfig.getConfigurationSection(keys));
            this.loadedMines.add(mine);
        }

        future.complete(null);
        return future;
    }


    @Override
    public synchronized Future<Void> unload(String identifier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Mine mine = get(identifier);
        if (mine == null) {
            future.complete(null);
            return future;
        }

        this.loadedMines.remove(mine);

        if (saveOperations.containsKey(mine.getName()))
            return saveOperations.get(mine.getName());

        mine.saveToConfigSection(minesConfig.createSection(mine.getName()));

        future.complete(null);
        return future;
    }

    @Override
    public Future<Void> remove(String identifier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        future.complete(null);

        Mine mine = get(identifier);
        if (mine == null) {
            return future;
        }
        this.unload(identifier);
        minesConfig.set(mine.getName(), null);
        return future;
    }

    public synchronized void queueSaveMineOperation(Mine mine) {
        mine.getIoLock().lock();
        if (!this.saveOperations.containsKey(mine.getName())) {
            Future<Void> future = service.submit(() -> {
                try {
                    Thread.sleep(1000);
                    mine.saveToConfigSection(minesConfig.createSection(mine.getName()));
                    synchronized (this) {
                        saveOperations.remove(mine.getName());
                        if(saveOperations.size() == 0) {
                            minesConfig.save(MineFiles.MINES_FILE);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            });
            this.saveOperations.put(mine.getName(), future);
        }
        mine.getIoLock().unlock();
    }
}
