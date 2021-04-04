package com.soraxus.prisons.mines.manager;

import com.soraxus.prisons.core.Manager;
import com.soraxus.prisons.mines.MineFiles;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.PrivateMineManager;
import com.soraxus.prisons.util.concurrent.ConcurrentBulkOperationQueue;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MineManager extends Manager<Mine, String> {
    public static MineManager instance;
    private ExecutorService service = Executors.newCachedThreadPool();
    private List<Mine> loadedMines = new ArrayList();
    private FileConfiguration minesConfig;
    private ConcurrentBulkOperationQueue<Runnable> saveQueue = new ConcurrentBulkOperationQueue<>(this::flushSaveQueue0);

    public MineManager() {
        this.minesConfig = YamlConfiguration.loadConfiguration(MineFiles.MINES_FILE);
        instance = this;
    }

    public synchronized List<Mine> getLoaded() {
        List<Mine> list = new ArrayList<>(this.loadedMines);
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
        CompletableFuture<Void> future = new CompletableFuture<>();

        for (String keys : this.minesConfig.getKeys(false)) {
            Mine mine = Mine.fromConfigSection(this.minesConfig.getConfigurationSection(keys));
            this.loadedMines.add(mine);
        }

        future.complete(null);
        return future;
    }

    public synchronized Future<Void> unload(String identifier) {
        Mine mine = this.get(identifier);
        if (mine != null) {
            //Unload
            this.loadedMines.removeIf((m) -> m.getName().endsWith(identifier));
            queueSaveMineOperation(mine);
        }
        return null;
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

    public void flushSaveQueue() {
        saveQueue.flushQueue();
    }

    private synchronized void flushSaveQueue0(List<Runnable> runnables) {
        runnables.forEach(Runnable::run);
        try {
            this.minesConfig.save(MineFiles.MINES_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void queueSaveMineOperation(Mine mine) {
        saveQueue.queue(() -> mine.saveToConfigSection(this.minesConfig.createSection(mine.getName())));
    }

    public FileConfiguration getMinesConfig() {
        return this.minesConfig;
    }
}