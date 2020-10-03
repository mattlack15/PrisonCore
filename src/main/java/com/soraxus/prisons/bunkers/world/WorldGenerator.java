package com.soraxus.prisons.bunkers.world;

import com.soraxus.prisons.util.Synchronizer;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@SuppressWarnings("unchecked")
public class WorldGenerator {
    private static Field craftBukkitWorldMap;

    private static final ReentrantLock safetyLock = new ReentrantLock(true); //Just a lock to make me feel safe in some places

    static {
        try {
            craftBukkitWorldMap = CraftServer.class.getDeclaredField("worlds");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static BunkerWorldServer createWorld(BunkerWorld world) {
        BunkerWorldServer worldServer = createNMSWorld(world);
        addWorldToServerList(worldServer);
        return worldServer;
    }

    private static final ReentrantLock sLock = new ReentrantLock(true);

    public static BunkerWorldServer createNMSWorld(BunkerWorld world) {
        BunkerWorldDataManager dataManager = new BunkerWorldDataManager(world);

        //Check for synchronized map
        safetyLock.lock();

        int dimension;
        try {
            craftBukkitWorldMap.setAccessible(true);
            Map<Object, Object> current = (Map<Object, Object>) craftBukkitWorldMap.get(Bukkit.getServer());
            if (!current.getClass().getName().contains("Synchronized")) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                Synchronizer.synchronize(() -> {
                    try {
                        craftBukkitWorldMap.setAccessible(true);
                        craftBukkitWorldMap.set(Bukkit.getServer(), Collections.synchronizedMap((Map<Object, Object>) craftBukkitWorldMap.get(Bukkit.getServer())));
                        craftBukkitWorldMap.setAccessible(false);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        future.complete(null);
                    }
                });
                future.get();
            }
            craftBukkitWorldMap.setAccessible(false);

            dimension = CraftWorld.CUSTOM_DIMENSION_OFFSET + Bukkit.getServer().getWorlds().size();
            boolean used = true;

            while (used) {
                for (World server : Bukkit.getServer().getWorlds()) { //Using getWorlds because that uses a concurrent map (was set to it earlier ^) and is *hopefully* safe
                    used = ((CraftWorld) server).getHandle().dimension == dimension;

                    if (used) {
                        dimension++;
                        break;
                    }
                }
            }
            return (BunkerWorldServer) new BunkerWorldServer(dataManager, dimension).b();
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            safetyLock.unlock();
        }
    }

    private static final ReentrantLock addLock = new ReentrantLock(true);

    public static void addWorldToServerList(BunkerWorldServer worldObject) {
        if (worldObject == null) {
            throw new IllegalArgumentException("World object must be an instance of WorldServer!");
        }

        worldObject.setReady(true);
        MinecraftServer mcServer = MinecraftServer.getServer();

        addLock.lock();
        try {
            if (mcServer.server.getWorld(worldObject.getWorld().getUID()) == null) {
                mcServer.server.addWorld(worldObject.getWorld());
            }
            if (!mcServer.worlds.contains(worldObject)) {
                mcServer.worlds.add(worldObject);
            }
        } finally {
            addLock.unlock();
        }

        //NOTE: It would seem calling these is necessary for certain spigot functions to work in this world
        //The one I encountered was falling blocks not caring for setDropItem(false)
        Synchronizer.synchronize(() -> {
                Bukkit.getPluginManager().callEvent(new WorldInitEvent(worldObject.getWorld()));
                Bukkit.getPluginManager().callEvent(new WorldLoadEvent(worldObject.getWorld()));
            });
    }
}