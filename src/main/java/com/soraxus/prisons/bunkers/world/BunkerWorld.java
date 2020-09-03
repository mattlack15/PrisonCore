package com.soraxus.prisons.bunkers.world;

import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.ElementGenerationSettings;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.util.time.Timer;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.Chunk;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Getter
public class BunkerWorld {
    private Bunker bunker;
    private BunkerWorldServer world;
    private BunkerAsyncWorld asyncWorld;

    private int sizeTiles;
    private int tileSize;
    private int borderChunks;
    private int sizeChunks;

    private ExecutorService service = Executors.newCachedThreadPool();
    @Getter
    private volatile boolean finished;

    @Getter
    private volatile boolean generated;

    public BunkerWorld(Bunker bunker, int sizeTiles, int tileSize, int borderChunks) {
        this.bunker = bunker;
        this.sizeTiles = sizeTiles;
        this.tileSize = tileSize;
        this.sizeChunks = ((sizeTiles * tileSize) >> 4) + borderChunks * 2 + 1;
        this.borderChunks = borderChunks;
        this.asyncWorld = new BunkerAsyncWorld();
    }

    public World getBukkitWorld() {
        return this.world.getWorld();
    }

    public void createWorld() {
        if (this.world != null) {
            throw new IllegalStateException("World already exists");
        }
        this.world = WorldGenerator.createNMSWorld(this);
    }

    public void finishCreation() {
        finished = false;
        WorldGenerator.addWorldToServerList(this.world);
        this.world.getWorld().setTime(23000); //Arbitrary
        finished = true;
    }

    public void generate() {
        generated = false;

        Schematic mapSchematic = bunker.getMapSchematic();

        if (mapSchematic == null) {
            throw new IllegalStateException("Could not find bunker map schematic!");
        }

        long ms = System.currentTimeMillis();
        try {
            asyncWorld.pasteSchematic(mapSchematic, new IntVector3D(0, 70, 0), true);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Unable to paste map schematic for bunker!");
        }

        bunker.setBunkerLocation(new IntVector3D(borderChunks * 16, 70, borderChunks * 16));

        Tile[][] tiles = bunker.getTileMap().getTiles();
        IntVector2D zero = new IntVector2D(0, 0);
        for (int x = 0; x < tiles.length; x++) {
            for (int z = 0; z < tiles.length; z++) {
                Tile tile = tiles[x][z];
                IntVector3D tileStart = new IntVector3D(x * tileSize + borderChunks * 16, 71, z * tileSize + borderChunks * 16);
                for (int x1 = tileStart.getX(), endx = tileStart.getX() + tileSize; x1 < endx; x1++) {
                    for (int z1 = tileStart.getZ(), endz = tileStart.getZ() + tileSize; z1 < endz; z1++) {
                        asyncWorld.setBiome(x1, z1, ((x & 1) ^ (z & 1)) == 1 ? 4 : 22);
                    }
                }
                if (tile == null) {
                    continue;
                }
                if (tile.getInternalPosition().equals(zero)) {
                    try {
                        ElementGenerationSettings settings = tile.getParent().getGenerationSettings();
                        if (settings.isNeedsBuilding())
                            tile.getParent().build(asyncWorld, false);
                    } catch(Exception e) {
                        e.printStackTrace();
                        System.out.println("ERROR while generating tile with parent of type: " + tile.getParent().getClass().getName());
                    }
                }
            }
        }

        if (this.world == null) {
            throw new IllegalStateException("World does not exist");
        }

        Timer t1 = new Timer();
        //Multi-Threaded to speed it up
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        asyncWorld.getChunkMap().getCachedCopy().forEach(c -> pool.submit(() -> {
            c.finish(world);
        }));

        while(true) if(pool.awaitQuiescence(1, TimeUnit.SECONDS)) break;

        this.world.getWorld().getWorldBorder().setSize(sizeTiles * tileSize + borderChunks * 16 * 2);
        this.world.getWorld().getWorldBorder().setCenter((sizeTiles * tileSize + borderChunks * 16 * 2) / 2D, (sizeTiles * tileSize + borderChunks * 16 * 2) / 2D);
        this.world.getWorld().getWorldBorder().setWarningDistance(0);
        this.world.getWorld().setGameRuleValue("doWeatherCycle", "false");

        generated = true;
    }

    public String getName() {
        return ModuleBunkers.WORLD_PREFIX + getBunker().getId().toString().replaceAll("-", "");
    }

    public IntVector2D getTileAt(Location loc) {
        return new IntVector2D((loc.getBlockX() - borderChunks * 16) / tileSize, (loc.getBlockZ() - borderChunks * 16) / tileSize);
    }

    public Chunk getChunk(int x, int z) {
        if (x < 0 || z < 0 || x > sizeChunks || z > sizeChunks) {
            return null;
        }
        if (asyncWorld.getChunkMap().get(x, z) == null)
            return null;
        BunkerAsyncChunk asyncChunk = asyncWorld.getChunk(x, z);
        asyncChunk.waitForFinish();
        return asyncChunk.getNmsStoredChunk();
    }

    public void unload() {
        while(Bukkit.getWorld(this.getName()) != null) {
            Bukkit.unloadWorld(this.getName(), false);
        }
        this.asyncWorld = new BunkerAsyncWorld();
    }
}