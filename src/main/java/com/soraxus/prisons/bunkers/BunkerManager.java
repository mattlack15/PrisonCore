package com.soraxus.prisons.bunkers;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.matchmaking.BunkerMatchMaker;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.bunkers.world.BunkerWorld;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.WeakList;
import com.soraxus.prisons.util.locks.SaveLoadLock;
import com.soraxus.prisons.util.maps.LockingMap;
import com.soraxus.prisons.util.time.Timer;
import lombok.Getter;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.compressors.ZstdCompressor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Management class for bunkers, manages loading and unloading of bunkers and creation/deletion of bunkers
 */
public class BunkerManager {

    public static final int TILE_SIZE_BLOCKS = 7;
    public static final int BUNKER_SIZE_TILES = 33;
    public static final int BORDER_WIDTH_CHUNKS = 3;
    public static BunkerManager instance;
    @Getter
    private final File baseDir;
    private final Map<UUID, Bunker> loadedBunkers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final SaveLoadLock<UUID, Bunker> ioLock = new SaveLoadLock<>();
    private final ExecutorService loadingService = Executors.newFixedThreadPool(20);

    @Getter
    private final WeakList<Bunker> bunkerWeakList = new WeakList<>();

    private final LockingMap<UUID, Double> cachedRatings = new LockingMap<>();

    protected volatile long lastTick = -1;

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
    }

    public LockingMap<UUID, Double> getCachedRatings() {
        return getCachedRatings(true);
    }

    public LockingMap<UUID, Double> getCachedRatings(boolean update) {
        if (update) {
            getLoadedBunkers().forEach((b) -> cachedRatings.put(b.getId(), b.getRating()));
        }
        return cachedRatings;
    }

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

    public List<Bunker> getLoadedBunkers() {
        lock.lock();
        List<Bunker> bunkers = new ArrayList<>(this.loadedBunkers.values());
        lock.unlock();
        return bunkers;
    }

    public Bunker getLoadedBunker(UUID id) {
        lock.lock();
        try {
            return loadedBunkers.get(id);
        } finally {
            lock.unlock();
        }
    }

    private CompletableFuture<Bunker> createBunker(Gang gang) {
        CompletableFuture<Bunker> future = new CompletableFuture<>();

        if (gang.getBunker() != null) {
            future.complete(gang.getBunker());
            return future;
        }


        loadingService.submit(() -> {
            Future<Bunker> bunkerFuture = ioLock.loadLock(gang.getId(), future);
            if (bunkerFuture != null) {
                return bunkerFuture.get();
            }
            try {

                //Concurrency

                long now = System.currentTimeMillis();
                long temp = now;

                //Create bunker
                Bunker bunker = new Bunker(gang, "bunkertheme1");

                //Create world
                initWorld(bunker);

                //Enable
                bunker.enable();

                //Add to loaded bunker list
                lock.lock();
                this.bunkerWeakList.add(bunker);
                this.loadedBunkers.put(bunker.getId(), bunker);
                lock.unlock();

                cachedRatings.put(bunker.getId(), bunker.getRating());

                long time = System.currentTimeMillis() - now;
                Bukkit.broadcastMessage(ChatColor.RED + "STAT > " + ChatColor.WHITE + "Took " + ChatColor.YELLOW + time + ChatColor.WHITE + "ms to create bunker asynchronously!");

                //Concurrency
                ioLock.loadUnlock(gang.getId(), bunker);

                return bunker;
            } catch (Exception e) {
                e.printStackTrace();
                ioLock.loadUnlock(gang.getId(), null);
                return null;
            }
        });
        return future;
    }

    public CompletableFuture<Bunker> loadBunkerAsync(UUID id) {
        lock.lock();
        try {
            CompletableFuture<Bunker> future = new CompletableFuture<>();
            Bunker b = this.getLoadedBunker(id);
            if (b != null) {
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
                    bunker.enable();

                    //Add to loaded bunker list
                    lock.lock();
                    bunkerWeakList.add(bunker);
                    this.loadedBunkers.put(id, bunker);
                    lock.unlock();

                    cachedRatings.put(bunker.getId(), bunker.getRating());

                    long time = System.currentTimeMillis() - now;
                    Bukkit.broadcastMessage(ChatColor.RED + "STAT > " + ChatColor.WHITE + "Took " + ChatColor.YELLOW + time + ChatColor.WHITE + "ms to load bunker asynchronously!");

                    ioLock.loadUnlock(id, bunker);
                } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Callable<Void> getUnloadOp(Bunker bunker) {
        return () -> {
            ioLock.saveLock(bunker.getId()); //Lock

            try {
                CompletableFuture<Void> future = new CompletableFuture<>();

                //Synchronous part
                Synchronizer.synchronize(() -> {
                    try {

                        //End matches (Synchronously)
                        if (bunker.getAttackingMatch() != null)
                            bunker.getAttackingMatch().end();
                        if (bunker.getDefendingMatch() != null)
                            bunker.getDefendingMatch().end();

                        //Unload world
                        if (bunker.getWorld().getWorld() != null) {
                            bunker.getWorld().getBukkitWorld().getPlayers().forEach(p -> {
                                if (!bunker.teleportBack(p))
                                    p.teleport(new Location(Bukkit.getWorld("world"), 0, 80, 0)); //TEMPORARILY WORLD
                            });
                        }
                        bunker.disable();
                    } finally {
                        try {
                            bunker.getWorld().unload();
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
            } finally {
                ioLock.saveUnlock(bunker.getId());
            }
        };
    }

    public Callable<Void> getSaveAndUnloadOp(Bunker bunker) {
        return () -> {
            ioLock.saveLock(bunker.getId()); //Lock
            try {
                getAsyncSaveOp(bunker).call();
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

    public Future<Void> saveAndUnloadBunker(Bunker bunker) {
        return loadingService.submit(getSaveAndUnloadOp(bunker));
    }

    public Future<Void> unloadBunker(Bunker bunker) {
        return loadingService.submit(getUnloadOp(bunker));
    }

    public Callable<Void> getAsyncSaveOp(Bunker bunker) {
        return () -> {
            //Concurrency
            ioLock.saveLock(bunker.getId());

            long ms = System.currentTimeMillis();
            File file = getFile(bunker.getId());
            try (FileOutputStream stream = new FileOutputStream(file)) {
                GravSerializer serializer = new GravSerializer();
                bunker.serialize(serializer);
                serializer.writeToStream(stream, ZstdCompressor.instance);

                cachedRatings.put(bunker.getId(), bunker.getRating());

                long time = System.currentTimeMillis() - ms;
                Bukkit.broadcastMessage(ChatColor.RED + "STAT > " + ChatColor.WHITE + "Took " + ChatColor.YELLOW + time + ChatColor.WHITE + "ms to save bunker asynchronously!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ioLock.saveUnlock(bunker.getId());
            }
            return null;
        };
    }

    public Future<Void> saveBunkerAsync(Bunker bunker) throws IllegalStateException {
        return loadingService.submit(getAsyncSaveOp(bunker));
    }

    /**
     * Warning: Will take AT LEAST 50ms to execute
     */
    private void initWorld(Bunker bunker) {
        BunkerWorld world = new BunkerWorld(bunker, BUNKER_SIZE_TILES, TILE_SIZE_BLOCKS, BORDER_WIDTH_CHUNKS);

        bunker.setWorld(world);

        AtomicLong sync = new AtomicLong();

        world.createWorld();

        Timer timer = new Timer();
        world.generate();

        CompletableFuture<Void> future = new CompletableFuture<>();
        Synchronizer.synchronize(() -> {
            try {
                long t = System.currentTimeMillis();
                world.finishCreation();
                t = System.currentTimeMillis() - t;
                sync.addAndGet(t);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                future.complete(null);
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void internalUnloadBunker(Bunker bunker) {
        this.lock.lock();
        loadedBunkers.remove(bunker.getId());
        this.lock.unlock();
    }

    private File getFile(UUID id) {
        return new File(baseDir, id.toString() + ".bunker");
    }

    void tick() { // TODO: Call this in a scheduler sync
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
    public Schematic getDefaultSchematic() {
        return BunkerSchematics.get("default-schematic");
    }
}
