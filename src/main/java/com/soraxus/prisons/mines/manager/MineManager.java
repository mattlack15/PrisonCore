package com.soraxus.prisons.mines.manager;

import com.soraxus.prisons.core.Manager;
import com.soraxus.prisons.mines.MineFiles;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.PrivateMineManager;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class MineManager extends Manager<Mine, String> {
    public static MineManager instance;
    private ExecutorService service = Executors.newCachedThreadPool();
    private List<Mine> loadedMines = new ArrayList();
    private FileConfiguration minesConfig;
    private ConcurrentMap<String, Future<Void>> saveOperations;

    public MineManager() {
        this.minesConfig = YamlConfiguration.loadConfiguration(MineFiles.MINES_FILE);
        this.saveOperations = new ConcurrentHashMap();
        instance = this;
    }

    public synchronized void joinSaveOps() {
        Iterator var1 = (new HashMap(this.saveOperations)).entrySet().iterator();

        while(var1.hasNext()) {
            Entry<String, Future<Void>> entry = (Entry)var1.next();
            Future v = (Future)entry.getValue();

            try {
                v.get();
            } catch (ExecutionException | InterruptedException var5) {
                var5.printStackTrace();
            }
        }

        this.saveOperations.clear();
    }

    public synchronized List<Mine> getLoaded() {
        List<Mine> list = new ArrayList(this.loadedMines);
        list.sort(Comparator.comparingInt(Mine::getOrder));
        return list;
    }

    public synchronized boolean add(Mine object) {
        if (this.get(object.getName()) != null) {
            return false;
        } else {
            this.loadedMines.add(object);
            this.queueSaveMineOperation(object);
            return true;
        }
    }

    public synchronized Mine get(String identifier) {
        Iterator var2 = this.loadedMines.iterator();

        Mine mines;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            mines = (Mine)var2.next();
        } while(!mines.getName().equalsIgnoreCase(identifier));

        return mines;
    }

    public synchronized Mine getMineOf(final Location location) {
        for (final Mine mines : this.loadedMines) {
            if (mines.getRegion().getWorld().equals(location.getWorld()) && mines.getRegion().contains(Vector3D.fromBukkitVector(location.toVector()))) {
                return mines;
            }
        }
        for (final PrivateMine mines2 : PrivateMineManager.instance.getLoadedPrivateMines()) {
            if (mines2.getWorld() == null) {
                continue;
            }
            if (mines2.getRegion().getWorld().equals(location.getWorld()) && mines2.getRegion().contains(Vector3D.fromBukkitVector(location.toVector()))) {
                return mines2;
            }
        }
        return null;
    }

    public synchronized Future<Mine> load(String identifier) {
        CompletableFuture<Mine> future = new CompletableFuture<>();
        if (!this.minesConfig.isConfigurationSection(identifier)) {
            future.complete(null);
        } else {
            ConfigurationSection section = this.minesConfig.getConfigurationSection(identifier);
            Mine mine = Mine.fromConfigSection(section);
            this.loadedMines.add(mine);
            future.complete(mine);
        }
        return future;
    }

    public synchronized Future<Void> loadAll() {
        CompletableFuture<Void> future = new CompletableFuture();

        for (String keys : this.minesConfig.getKeys(false)) {
            Mine mine = Mine.fromConfigSection(this.minesConfig.getConfigurationSection(keys));
            this.loadedMines.add(mine);
        }

        future.complete(null);
        return future;
    }

    public synchronized Future<Void> unload(String identifier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Mine mine = this.get(identifier);
        if (mine == null) {
            future.complete(null);
            return future;
        } else {
            this.loadedMines.removeIf((m) -> {
                return m.getName().endsWith(identifier);
            });
            if (this.saveOperations.containsKey(mine.getName())) {
                return this.saveOperations.get(mine.getName());
            } else {
                mine.saveToConfigSection(this.minesConfig.createSection(mine.getName()));
                future.complete(null);
                return future;
            }
        }
    }

    public Future<Void> remove(String identifier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        Mine mine = this.get(identifier);
        if (mine == null) {
            return future;
        } else {
            synchronized(this) {
                this.unload(identifier);
                this.minesConfig.set(mine.getName(), null);

                try {
                    this.minesConfig.save(MineFiles.MINES_FILE);
                } catch (IOException var7) {
                    var7.printStackTrace();
                }

                return future;
            }
        }
    }

    public synchronized void queueSaveMineOperation(Mine mine) {
        if (mine.shouldSave()) {
            mine.getIoLock().lock();
            if (!this.saveOperations.containsKey(mine.getName())) {
                Future<Void> future = this.service.submit(() -> {
                    try {
                        Thread.sleep(1000L);
                        if (!this.getLoaded().contains(mine)) {
                            return null;
                        }

                        mine.saveToConfigSection(this.minesConfig.createSection(mine.getName()));
                        synchronized(this) {
                            this.saveOperations.remove(mine.getName());
                            if (this.saveOperations.size() == 0) {
                                this.minesConfig.save(MineFiles.MINES_FILE);
                            }
                        }
                    } catch (InterruptedException var5) {
                        var5.printStackTrace();
                    }

                    return null;
                });
                this.saveOperations.put(mine.getName(), future);
            }

            mine.getIoLock().unlock();
        }
    }

    public FileConfiguration getMinesConfig() {
        return this.minesConfig;
    }
}