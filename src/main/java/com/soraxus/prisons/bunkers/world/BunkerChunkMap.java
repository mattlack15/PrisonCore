package com.soraxus.prisons.bunkers.world;

import java.util.ArrayList;
import java.util.List;

public class BunkerChunkMap {
    private List<BunkerAsyncChunk> chunks = new ArrayList<>();

    private BunkerAsyncWorld parent;

    public BunkerChunkMap(BunkerAsyncWorld parent) {
        this.parent = parent;
    }

    public synchronized List<BunkerAsyncChunk> getCachedCopy() {
        return new ArrayList<>(chunks);
    }

//    public boolean contains(ChunkLocation location) {
//        for (BunkerAsyncChunk chunk : chunks) {
//            if (chunk.getLocation().getX() == location.getX() && chunk.getLocation().getZ() == location.getZ())
//                return true;
//        }
//        return false;
//    }

    public synchronized void clear() {
        this.chunks.clear();
    }

    public synchronized BunkerAsyncChunk get(int cx, int cz) {
        for (BunkerAsyncChunk chunk : chunks) {
            if (chunk.getX() == cx && chunk.getZ() == cz)
                return chunk;
        }
        return null;
    }

    public synchronized void remove(int cx, int cz) {
        this.chunks.removeIf(c -> c.getX() == cx && c.getZ() == cz);
    }

    public synchronized BunkerAsyncChunk getOrMake(int cx, int cz) {
        BunkerAsyncChunk chunk = this.get(cx, cz);

        if (chunk == null) {
            chunk = parent.getNewChunk(cx, cz);
            chunks.add(chunk);
        }
        return chunk;
    }

//    public synchronized void add(BunkerAsyncChunk chunk) {
//        if (!this.contains(chunk.getLoc()))
//            this.chunks.add(chunk);
//    }
}
