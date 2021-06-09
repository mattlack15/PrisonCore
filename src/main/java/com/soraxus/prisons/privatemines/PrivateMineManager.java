package com.soraxus.prisons.privatemines;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.list.ListUtil;
import com.soraxus.prisons.util.locks.ManagerLock;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class PrivateMineManager {

    public static PrivateMineManager instance;

    private final List<PrivateMine> loadedPrivateMines = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final ManagerLock<UUID, PrivateMine> ioLock = new ManagerLock<>();
    private final ExecutorService loadingService = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setUncaughtExceptionHandler((e, e1) -> e1.printStackTrace()).build());

    private final List<Schematic> schematicList = new ArrayList<>();

    private final File baseDir;

    private final Map<UUID, CachedMineInfo> cachedMineInfos = new ConcurrentHashMap<>();
    private long lastCacheUpdate = System.currentTimeMillis();

    public PrivateMineManager(File baseDir) {
        instance = this;
        this.baseDir = baseDir;
        baseDir.mkdirs();
        try {
            this.schematicList.add(new Schematic(new File(ModulePrivateMines.instance.getDataFolder(), "pm.bschem")));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not find, or could not load private mine schematic!");
        }

        //Cache
        try {
            this.cachedMineInfos.putAll(new GravSerializer(new FileInputStream(new File(baseDir, "mine_cache.cache"))).readObject());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not find, or could not load private mine cache!");
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(SpigotPrisonCore.instance, this::tick, 0, 1);
    }

    public File getFile(UUID id) {
        return new File(baseDir, id.toString());
    }

    public void saveCache() {
        File file = new File(baseDir, "mine_cache.cache");
        GravSerializer serializer = new GravSerializer();
        serializer.writeObject(this.cachedMineInfos);
        try {
            serializer.writeToStream(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cache(PrivateMine mine) {
        this.cachedMineInfos.put(mine.getGang().getId(), new CachedMineInfo(
                mine.getGang().getName(),
                mine.getVisitationManager().getRentalPrice(),
                mine.getVisitationManager().getAtomicRentedSlots().get(),
                mine.getVisitationManager().getSlots(),
                mine.getRank()));
    }

    public Map<UUID, CachedMineInfo> getCachedMineInfos() {
        return getCachedMineInfos(true);
    }

    public Map<UUID, CachedMineInfo> getCachedMineInfos(boolean updated) {
        if (updated) {
            if (System.currentTimeMillis() - lastCacheUpdate > 100) { //Technically not synchronized, but is safe to be executed concurrently
                lastCacheUpdate = 0;
                this.getLoadedPrivateMines().forEach(this::cache); //This is safe to be executed concurrently
            }
        }
        return new HashMap<>(this.cachedMineInfos);
    }

    public List<PrivateMine> getLoadedPrivateMines() {
        lock.lock();
        try {
            return new ArrayList<>(this.loadedPrivateMines);
        } finally {
            lock.unlock();
        }
    }

    private Supplier<PrivateMine> getUnlockedCreationOp(Gang gang) { //The unlocked operations are just so that an operation can pivot to become a load or creation without unlocking
        return () -> { //This is really only needed for creation, because creation should pivot to a load if it already exists
            PrivateMine mine;
            //Check if already loaded
            mine = getLoadedPrivateMine(gang.getId());
            if (mine != null) {
                return mine;
            }

            //Commence creation
            PrivateMine halfCreatedMine = new PrivateMine(gang);
            halfCreatedMine.create(getRandomMineSchematic(), new CuboidRegion(null,
                    new Vector3D(7, 5, 7), new Vector3D(80 - 8, 79, 80 - 8)));

            mine = halfCreatedMine;

            //Add to list
            lock.lock();
            try {
                this.loadedPrivateMines.add(mine);
            } finally {
                lock.unlock();
            }
            return mine;
        };
    }

    private Supplier<PrivateMine> getUnlockedLoadOp(Gang gang) {
        return () -> {
            try {

                PrivateMine mine;

                File file = getFile(gang.getId());
                if (!file.exists())
                    return null;

                GravSerializer serializer = new GravSerializer(new FileInputStream(file));

                PrivateMine halfLoadedMine = PrivateMine.deserialize(serializer, gang);
                halfLoadedMine.create(getRandomMineSchematic(), new CuboidRegion(null,
                        new Vector3D(7, 5, 7), new Vector3D(80 - 8, 79, 80 - 8)));

                mine = halfLoadedMine;

                //Add to list
                lock.lock();
                try {
                    loadedPrivateMines.add(mine);
                } finally {
                    lock.unlock();
                }
                return mine;
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        };
    }

    public CompletableFuture<PrivateMine> createOrLoadPrivateMineAsync(@NotNull Gang gang) {
        CompletableFuture<PrivateMine> future = new CompletableFuture<>();

        PrivateMine current = getLoadedPrivateMine(gang.getId());
        if (current != null) {
            future.complete(current);
            return future;
        }

        //Submit task
        loadingService.submit(() -> {

            PrivateMine otMine = ioLock.creationLock(gang.getId(), future);
            if (otMine != null) { //Lock not acquired, but loading/creation completed by another thread
                future.complete(otMine);
                return;
            }

            //Lock acquired
            PrivateMine mine = null;
            try {
                File file = getFile(gang.getId());
                if (file.exists()) {
                    //Mine already exists, complete a load operation
                    long ms = System.currentTimeMillis();
                    mine = getUnlockedLoadOp(gang).get();
                    ms = System.currentTimeMillis() - ms;

                    if(mine == null) {
                        ms = System.currentTimeMillis();
                        mine = getUnlockedCreationOp(gang).get();
                        ms = System.currentTimeMillis() - ms;
                        System.out.println("Created mine in " + ms + "ms");
                    } else {
                        System.out.println("Loaded mine in " + ms + "ms");
                    }

                } else {
                    //Mine doesn't exist, complete a creation operation
                    long ms = System.currentTimeMillis();
                    mine = getUnlockedCreationOp(gang).get();
                    ms = System.currentTimeMillis() - ms;
                    System.out.println("Created mine in " + ms + "ms");
                }
                ioLock.creationUnlock(gang.getId(), mine); //Works for both creation and loading
            } catch (Throwable t) {
                t.printStackTrace();
                ioLock.creationUnlock(gang.getId(), null); //Works for both creation and loading
            }
        });

        return future;
    }

    public CompletableFuture<PrivateMine> loadPrivateMineAsync(Gang gang) {
        CompletableFuture<PrivateMine> future = new CompletableFuture<>();
        PrivateMine m = getLoadedPrivateMine(gang.getId());
        if (m != null) {
            future.complete(m);
            return future;
        }

        loadingService.submit(() -> {
            CompletableFuture<PrivateMine> current = ioLock.loadLock(gang.getId(), future);
            if (current != null) {
                future.complete(current.join());
                return;
            }

            //Acquired lock
            PrivateMine mine = null;
            try {
                mine = getUnlockedLoadOp(gang).get();
            } finally {
                ioLock.loadUnlock(gang.getId(), mine);
            }
        });
        return future;
    }

    public Runnable getSaveOp(PrivateMine mine) {
        return () -> {
            ioLock.saveLock(mine.getGang().getId());
            try {
                GravSerializer serializer = new GravSerializer();
                mine.serialize(serializer);
                File file = getFile(mine.getGang().getId());
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                try {
                    serializer.writeToStream(new FileOutputStream(file));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } finally {
                ioLock.saveUnlock(mine.getGang().getId());
            }
        };
    }

    public Supplier<CompletableFuture<Void>> getUnloadOp(PrivateMine mine) {
        return () -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Synchronizer.synchronize(() -> {
                mine.unloadWorld();
                lock.lock();
                try {
                    this.loadedPrivateMines.remove(mine);
                } finally {
                    lock.unlock();
                }
                this.cache(mine);
                future.complete(null);
            });
            return future;
        };
    }

    public CompletableFuture<Void> saveAndUnloadPrivateMineAsync(PrivateMine mine) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        getUnloadOp(mine).get();
        loadingService.submit(() -> {
            getSaveOp(mine).run();
            future.complete(null);
        });
        return future;
    }

    public void saveAndUnloadPrivateMineSync(PrivateMine mine) {
        CompletableFuture<Void> future = getUnloadOp(mine).get();
        getSaveOp(mine).run();
        future.join();
    }

    public void deletePrivateMine(PrivateMine mine) {
        if (mine == null)
            return;
        this.getUnloadOp(mine).get();
        this.cachedMineInfos.remove(mine.getGang().getId());
        getFile(mine.getGang().getId()).delete();
    }

    public PrivateMine getLoadedPrivateMine(UUID id) {
        lock.lock();
        try {
            for (PrivateMine privateMine : loadedPrivateMines) {
                if (privateMine.getGang().getId().equals(id))
                    return privateMine;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public Schematic getRandomMineSchematic() {
        return ListUtil.randomElement(schematicList);
    }

    public void tick() {
        this.getLoadedPrivateMines().forEach(PrivateMine::tick);
    }

}
