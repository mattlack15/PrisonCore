package com.soraxus.prisons.bunkers.world;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.ElementGenerationSettings;
import com.soraxus.prisons.bunkers.base.Tile;
import lombok.Getter;
import net.ultragrav.asyncworld.customworld.SpigotCustomWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.Location;

@Getter
public class BunkerWorld extends SpigotCustomWorld {
    private Bunker bunker;

    private int sizeTiles;
    private int tileSize;
    private int borderChunks;
    private int sizeChunks;

    public BunkerWorld(Bunker bunker, int sizeTiles, int tileSize, int borderChunks) {
        super(SpigotPrisonCore.instance, ModuleBunkers.WORLD_PREFIX + bunker.getId().toString().replaceAll("-", ""),
                ((sizeTiles * tileSize) >> 4) + borderChunks * 2 + 1,
                ((sizeTiles * tileSize) >> 4) + borderChunks * 2 + 1);
        this.bunker = bunker;
        this.sizeTiles = sizeTiles;
        this.tileSize = tileSize;
        this.sizeChunks = ((sizeTiles * tileSize) >> 4) + borderChunks * 2 + 1;
        this.borderChunks = borderChunks;
    }

    public void generate() {
        super.create((asyncWorld) -> {

            Schematic mapSchematic = bunker.getMapSchematic();

            if (mapSchematic == null) {
                throw new IllegalStateException("Could not find bunker map schematic!");
            }

            try {
                asyncWorld.pasteSchematic(mapSchematic, new IntVector3D(0, 70, 0), true);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Unable to paste map schematic for bunker!");
            }

            bunker.setBunkerLocation(new IntVector3D(borderChunks * 16, 70, borderChunks * 16));

            long totalBiomeTime = 0L;
            long totalElementTime = 0L;

            Tile[][] tiles = bunker.getTileMap().getTiles();
            IntVector2D zero = new IntVector2D(0, 0);
            for (int x = 0; x < tiles.length; x++) {
                for (int z = 0; z < tiles.length; z++) {
                    Tile tile = tiles[x][z];
                    IntVector3D tileStart = new IntVector3D(x * tileSize + borderChunks * 16, 71, z * tileSize + borderChunks * 16);

                    long ms = System.currentTimeMillis();

                    for (int x1 = tileStart.getX(), endx = tileStart.getX() + tileSize; x1 < endx; x1++) {
                        for (int z1 = tileStart.getZ(), endz = tileStart.getZ() + tileSize; z1 < endz; z1++) {
                            asyncWorld.setBiome(x1, z1, 22);
                        }
                    }

                    ms = System.currentTimeMillis() - ms;
                    totalBiomeTime += ms;

                    if (tile == null) {
                        continue;
                    }

                    ms = System.currentTimeMillis();

                    if (tile.getInternalPosition().equals(zero)) {
                        try {
                            ElementGenerationSettings settings = tile.getParent().getGenerationSettings();
                            if (settings.isNeedsBuilding()) {
                                tile.getParent().build(asyncWorld, false);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            System.out.println("ERROR while generating tile with parent of type: " + tile.getParent().getClass().getName());
                        }
                    }

                    ms = System.currentTimeMillis() - ms;
                    totalElementTime += ms;
                }
            }
        });
        this.getBukkitWorld().getWorldBorder().setSize(sizeTiles * tileSize + borderChunks * 16 * 2);
        this.getBukkitWorld().getWorldBorder().setCenter((sizeTiles * tileSize + borderChunks * 16 * 2) / 2D, (sizeTiles * tileSize + borderChunks * 16 * 2) / 2D);
        this.getBukkitWorld().getWorldBorder().setWarningDistance(0);
        this.getBukkitWorld().setGameRuleValue("doWeatherCycle", "false");
        this.getBukkitWorld().setPVP(false);
        this.getBukkitWorld().setSpawnFlags(false, false);
        this.getBukkitWorld().setGameRuleValue("randomTickSpeed", "0");
        this.getBukkitWorld().setTime(23000);
    }

    public IntVector2D getTileAt(Location loc) {
        return new IntVector2D((loc.getBlockX() - borderChunks * 16) / tileSize, (loc.getBlockZ() - borderChunks * 16) / tileSize);
    }
}