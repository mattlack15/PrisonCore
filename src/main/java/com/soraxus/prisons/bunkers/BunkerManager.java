package com.soraxus.prisons.bunkers;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.matchmaking.BunkerMatchMaker;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.bunkers.world.BunkerWorld;
import com.soraxus.prisons.event.bunkers.AsyncBunkerCreationEvent;
import com.soraxus.prisons.event.bunkers.AsyncBunkerLoadEvent;
import com.soraxus.prisons.event.bunkers.BunkerSaveEvent;
import com.soraxus.prisons.event.bunkers.BunkerUnloadEvent;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.WeakList;
import com.soraxus.prisons.util.locks.ManagerLock;
import com.soraxus.prisons.util.maps.LockingMap;
import lombok.Getter;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.compressors.ZstdCompressor;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Event;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Management class for bunkers, manages loading and unloading of bunkers and creation/deletion of bunkers
 */
public class BunkerManager {

    public static final int TILE_SIZE_BLOCKS = 7;
    public static final int BUNKER_SIZE_TILES = 33;
    public static final int BORDER_WIDTH_CHUNKS = 4;
    public static BunkerManager instance;
    @Getter
    private final File baseDir;
    private final Map<UUID, Bunker> loadedBunkers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final ManagerLock<UUID, Bunker> ioLock = new ManagerLock<>();
    protected final ExecutorService loadingService = Executors.newFixedThreadPool(20);

    @Getter
    private final WeakList<Bunker> bunkerWeakList = new WeakList<>();

    private final LockingMap<UUID, Double> cachedRatings = new LockingMap<>();

    private final AtomicBoolean ignoreFirstAvg = new AtomicBoolean(true);

    public volatile long lastTick = -1;

    public BunkerManager(File bunkerFolder) {
        instance = this;
        bunkerFolder.mkdirs();
        this.baseDir = bunkerFolder;

        //Crash detector
        new Thread(() -> {
            try {
                while (true) {
                    long l = lastTick;
                    if (l == -2)
                        return;
                    if (l != -1 && (System.currentTimeMillis() - l) > 4000) {
                        File file = new File(SpigotPrisonCore.instance.getDataFolder(), "Crash-" + UUID.randomUUID().toString() + new Date().toString() + ".txt");
                        try (FileWriter writer = new FileWriter(file)) {
                            Map<?, ?> liveThreads = Thread.getAllStackTraces();
                            for (Object o : liveThreads.keySet()) {
                                Thread key = (Thread) o;
                                writer.append("Thread Name: ").append(key.getName()).append("\n");
                                writer.append("Status: ").append(key.getState().toString()).append("\n");
                                StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
                                for (StackTraceElement stackTraceElement : trace) {
                                    writer.append("\tat ").append(String.valueOf(stackTraceElement)).append("\n");
                                }
                            }
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        //Load cached ratings
        try {
            GravSerializer serializer = new GravSerializer(new FileInputStream(new File(bunkerFolder, "cachedRatings.cache")));
            for (int i = 0, length = serializer.readInt(); i < length; i++) {
                cachedRatings.put(serializer.readUUID(), serializer.readDouble());
            }
        } catch (Exception e) {
            try {
                ModuleBunkers.messageDevs("Creating new CachedRatings file");
                new File(bunkerFolder, "cachedRatings.cache").createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        //Make sure the save lock class is loaded before disabling
        UUID id = UUID.randomUUID();
        ioLock.saveLock(id);
        ioLock.saveUnlock(id);
    }

    /**
     * Get the cached ratings
     */
    public LockingMap<UUID, Double> getCachedRatings() {
        return getCachedRatings(true);
    }

    /**
     * Update the cached ratings then return the cached ratings
     */
    public LockingMap<UUID, Double> getCachedRatings(boolean update) {
        if (update) {
            getLoadedBunkers().forEach((b) -> cachedRatings.put(b.getId(), b.getRating()));
        }
        return cachedRatings;
    }

    /**
     * Save the cached ratings file
     */
    public void saveCachedRatings() {
        GravSerializer serializer = new GravSerializer();
        LockingMap<UUID, Double> ratings = cachedRatings.copy();
        serializer.writeInt(ratings.size());
        ratings.forEach((b, r) -> {
            serializer.writeUUID(b);
            serializer.writeDouble(r);
        });
        try {
            serializer.writeToStream(new FileOutputStream(new File(baseDir, "cachedRatings.cache")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all loaded bunkers
     */
    public List<Bunker> getLoadedBunkers() {
        lock.lock();
        List<Bunker> bunkers = new ArrayList<>(this.loadedBunkers.values());
        lock.unlock();
        return bunkers;
    }

    /**
     * Get a bunker that is currently loaded
     */
    public Bunker getLoadedBunker(UUID id) {
        lock.lock();
        try {
            return loadedBunkers.get(id);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Create a bunker asynchronously
     */
    private CompletableFuture<Bunker> createBunker(Gang gang) {
        CompletableFuture<Bunker> future = new CompletableFuture<>();

        lock.lock();
        try {
            Bunker b;
            if ((b = gang.getBunker()) != null) {
                future.complete(b);
                return future;
            }


            loadingService.submit(() -> {

                //Loop in order to acquire, in case the loading fails
                while (true) {
                    CompletableFuture<Bunker> bunkerFuture = ioLock.loadLock(gang.getId(), future);
                    if (bunkerFuture != null) {
                        Bunker bunker = bunkerFuture.join();
                        if (bunker == null) //Load failure - Try to create again
                            continue;
                        future.complete(bunker);
                        return; //Load/Creation successful
                    }
                    break; //Acquired
                }

                //Check once again
                Bunker b1;
                if ((b1 = gang.getBunker()) != null) {
                    ioLock.loadUnlock(gang.getId(), b1);
                    return;
                }

                try {

                    //Concurrency

                    long now = System.currentTimeMillis();

                    //Create bunker
                    Bunker bunker = new Bunker(gang, "bunkertheme1");

                    //Create world
                    initWorld(bunker);

                    //Enable
                    Synchronizer.synchronizeAndWait(bunker::enable);

                    //Add to loaded bunker list
                    lock.lock();
                    this.bunkerWeakList.add(bunker);
                    this.loadedBunkers.put(bunker.getId(), bunker);
                    lock.unlock();

                    cachedRatings.put(bunker.getId(), bunker.getRating());

                    AsyncBunkerCreationEvent event = new AsyncBunkerCreationEvent(bunker);
                    Bukkit.getPluginManager().callEvent(event);

                    long time = System.currentTimeMillis() - now;
                    Bukkit.broadcastMessage(ChatColor.RED + "STAT > " + ChatColor.WHITE + "Took " + ChatColor.YELLOW + time + ChatColor.WHITE + "ms to create bunker asynchronously!");

                    //Concurrency
                    ioLock.loadUnlock(gang.getId(), bunker);
                } catch (Exception e) {
                    e.printStackTrace();
                    ioLock.loadUnlock(gang.getId(), null);
                }
            });
            return future;
        } finally {
            lock.unlock();
        }
    }

    public void syncDeleteBunker(UUID id) {
        ioLock.saveLock(id);
        try {
            Bunker bunker = getLoadedBunker(id);
            if (bunker != null) {
                try {
                    getUnloadOp(bunker).call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.cachedRatings.remove(id);
            this.getFile(id).delete();
        } finally {
            ioLock.saveUnlock(id);
        }
    }

    public CompletableFuture<Void> deleteBunkerAsync(UUID id) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.loadingService.submit(() -> {
            this.syncDeleteBunker(id);
            future.complete(null);
        });
        return future;
    }

    /**
     * Load a bunker asynchronously
     */
    public CompletableFuture<Bunker> loadBunkerAsync(UUID id) {
        lock.lock();
        try {
            CompletableFuture<Bunker> future = new CompletableFuture<>();
            Bunker b = this.getLoadedBunker(id);
            if (b != null || id == null) {
                future.complete(b);
                return future;
            }

            loadingService.submit(() -> {

                //Concurrency
                Future<Bunker> bunkerFuture = ioLock.loadLock(id, future);
                if (bunkerFuture != null) {
                    try {
                        future.complete(bunkerFuture.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    } finally {

                        if (!future.isDone()) {
                            future.complete(null);
                        }
                    }
                    return;
                }

                File file = getFile(id);
                if (!file.exists()) {
                    future.complete(null);
                    ioLock.loadUnlock(id, null);
                    return;
                }

                try (FileInputStream stream = new FileInputStream(file)) {
                    long now = System.currentTimeMillis();

                    GravSerializer serializer = new GravSerializer(stream, ZstdCompressor.instance);
                    Bunker bunker = new Bunker(serializer);

                    //Create world
                    initWorld(bunker);

                    //Enable
                    Synchronizer.synchronizeAndWait(bunker::enable);

                    //Add to loaded bunker list
                    lock.lock();
                    bunkerWeakList.add(bunker);
                    this.loadedBunkers.put(id, bunker);
                    lock.unlock();

                    cachedRatings.put(bunker.getId(), bunker.getRating());

                    AsyncBunkerLoadEvent event = new AsyncBunkerLoadEvent(bunker);
                    Bukkit.getPluginManager().callEvent(event);

                    long time = System.currentTimeMillis() - now;
                    Bukkit.broadcastMessage(ChatColor.RED + "STAT > " + ChatColor.WHITE + "Took " + ChatColor.YELLOW + time + ChatColor.WHITE + "ms to load bunker asynchronously!");

                    if (!ignoreFirstAvg.compareAndSet(true, false))
                        BunkerDebugStats.measureMap.get(BunkerDebugStats.DebugStat.BUNKER_LOAD_TOTAL).addEntry(time);

                    ioLock.loadUnlock(id, bunker);
                } catch (Throwable e) {
                    ModuleBunkers.messageDevs(id + " Could not load bunker, see console");
                    future.complete(null);
                    e.printStackTrace();
                    ioLock.loadUnlock(id, null);
                }
            });
            return future;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Load or create a bunker if it does not exist
     */
    public CompletableFuture<Bunker> createOrLoadBunker(Gang gang) {
        CompletableFuture<Bunker> result = new CompletableFuture<>();
        loadingService.submit(() -> {
            try {
                Bunker bunker = loadBunkerAsync(gang.getId()).get();
                if (bunker == null) {
                    bunker = createBunker(gang).get();
                }
                result.complete(bunker);
            } catch (Exception e) {
                e.printStackTrace();
                result.complete(null);
            }
        });
        return result;
    }

    /**
     * This is mainly for onDisable
     */
    public void saveAndUnloadBunkerSync(Bunker bunker) {
        try {
            getSaveAndUnloadOp(bunker).call();
            Event event = new BunkerSaveEvent(bunker, false);
            Bukkit.getPluginManager().callEvent(event);
            event = new BunkerUnloadEvent(bunker, false);
            Bukkit.getPluginManager().callEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the operation to unload a bunker
     */
    public Callable<Void> getUnloadOp(Bunker bunker) {
        return () -> {

            try {

                CompletableFuture<Void> future = new CompletableFuture<>();

                //Also it's pretty much all synchronous

                Synchronizer.synchronize(() -> {
                    try {

                        //End matches (Synchronously)
                        if (bunker.getAttackingMatch() != null)
                            bunker.getAttackingMatch().end();
                        if (bunker.getDefendingMatch() != null)
                            bunker.getDefendingMatch().end();

                        bunker.disable();

                        //Unload world
                        if (bunker.getWorld().getBukkitWorld() != null) {
                            bunker.getWorld().getBukkitWorld().getPlayers().forEach(p -> {
                                if (!bunker.teleportBack(p))
                                    p.teleport(new Location(Bukkit.getWorld("world"), 0, 80, 0)); //TEMPORARILY WORLD
                            });
                        }
                    } finally {
                        try {
                            String name = bunker.getWorld().getBukkitWorld().getName();
                            bunker.getWorld().unload();
                            if(Bukkit.getWorld(name) != null)
                                ModuleBunkers.messageDevs("Could not unload bunker!");
                        } finally {
                            internalUnloadBunker(bunker);
                            future.complete(null);
                        }
                    }
                });

                try {
                    //complete the future
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    /**
     * Get the operation to save and unload a bunker
     */
    public Callable<Void> getSaveAndUnloadOp(Bunker bunker) {
        return () -> {
            ioLock.saveLock(bunker.getId()); //Lock
            try {
                getBunkerSaveOp(bunker, false).call();
                getUnloadOp(bunker).call();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                ioLock.saveUnlock(bunker.getId());
            }
        };
    }

    /**
     * Save and unload a bunker, unloading is synchronous, saving is async and completes the returned future
     */
    public Future<Void> saveAndUnloadBunker(Bunker bunker) {
        return loadingService.submit(() -> {
            getSaveAndUnloadOp(bunker).call();
            Event event = new BunkerSaveEvent(bunker, false);
            Bukkit.getPluginManager().callEvent(event);
            event = new BunkerUnloadEvent(bunker, false);
            Bukkit.getPluginManager().callEvent(event);
            return null;
        });
    }

    /**
     * Unload a bunker
     */
    public Future<Void> unloadBunker(Bunker bunker) {
        return loadingService.submit(getUnloadOp(bunker));
    }

    /**
     * Get the operation to save a bunker
     */
    public Callable<Void> getBunkerSaveOp(Bunker bunker, boolean locked) {
        return () -> {
            long ms = System.currentTimeMillis();
            File file = getFile(bunker.getId());
            if (!file.exists())
                file.createNewFile();

            GravSerializer serializer = new GravSerializer();
            bunker.serialize(serializer);

            if (locked)
                ioLock.saveLock(bunker.getId());

            try (FileOutputStream stream = new FileOutputStream(file)) {

                serializer.writeToStream(stream, ZstdCompressor.instance);

                cachedRatings.put(bunker.getId(), bunker.getRating());

                long time = System.currentTimeMillis() - ms;
                Bukkit.broadcastMessage(ChatColor.RED + "STAT > " + ChatColor.WHITE + "Took " + ChatColor.YELLOW + time + ChatColor.WHITE + "ms to save bunker asynchronously!");
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (locked)
                    ioLock.saveUnlock(bunker.getId());
            }
            return null;
        };
    }

    /**
     * Save a bunker asynchronously
     */
    public Future<Void> saveBunkerAsync(Bunker bunker) throws IllegalStateException {
        return loadingService.submit(() -> {
            getBunkerSaveOp(bunker, true).call();
            BunkerSaveEvent event = new BunkerSaveEvent(bunker, true);
            Bukkit.getPluginManager().callEvent(event);
            return null;
        });
    }

    /**
     * Creates the world of a bunker
     * Warning: Will take AT LEAST 50ms to execute
     */
    private void initWorld(Bunker bunker) {
        BunkerWorld world = new BunkerWorld(bunker, BUNKER_SIZE_TILES, TILE_SIZE_BLOCKS, BORDER_WIDTH_CHUNKS);

        world.generate();

        Bunker.setWorld(bunker, world);
    }

    private void internalUnloadBunker(Bunker bunker) {
        BunkerUnloadEvent event = new BunkerUnloadEvent(bunker, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        this.lock.lock();
        loadedBunkers.remove(bunker.getId());
        this.lock.unlock();
    }

    /**
     * Get the file for a bunker
     */
    public File getFile(UUID id) {
        return new File(baseDir, id.toString() + ".bunker");
    }

    /**
     * Tick all bunkers and matches
     */
    void tick() {
        lastTick = System.currentTimeMillis();
        for (Bunker bunker : loadedBunkers.values()) {
            if (bunker.shouldTick()) {
                bunker.tick();
            }
        }
        BunkerMatchMaker.instance.tick();
    }

    /**
     * Get the default schematic for an element with a schematic that could not be found
     */
    public Schematic getDefaultSchematic(IntVector2D shape) {
        return BunkerSchematics.getDefaultSchematic(shape);
    }

    public boolean tryUnload(Bunker loadedBunker) {
        return tryUnload(loadedBunker, true);
    }

    public boolean tryUnload(Bunker loadedBunker, boolean save) {
        if (loadedBunker == null)
            return false;
        if (GangManager.instance.getLoadedGang(loadedBunker.getId()) != null)
            return false;
        if (this.getLoadedBunker(loadedBunker.getId()) == null)
            return false;
        if (save)
            this.saveAndUnloadBunker(loadedBunker);
        else
            this.unloadBunker(loadedBunker);
        return true;
    }
}
