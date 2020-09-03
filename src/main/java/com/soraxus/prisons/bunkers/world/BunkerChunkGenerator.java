package com.soraxus.prisons.bunkers.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class BunkerChunkGenerator extends ChunkGenerator {
    private static final byte[] empty = new byte[16*16*256];

    public byte[] generate(World world, Random random, int x, int z) {
        return empty;
    }
}
