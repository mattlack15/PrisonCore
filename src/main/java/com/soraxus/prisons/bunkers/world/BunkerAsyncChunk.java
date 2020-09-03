package com.soraxus.prisons.bunkers.world;

import net.minecraft.server.v1_12_R1.*;
import net.ultragrav.asyncworld.AsyncChunk;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.ChunkLocation;
import net.ultragrav.asyncworld.nbt.*;

import java.util.Arrays;
import java.util.Map;

public class BunkerAsyncChunk extends AsyncChunk {
    private final int cx;
    private final int cz;
    private Chunk nmsStoredChunk;
    private volatile boolean finished = false;
    private final ChunkSection[] sections = new ChunkSection[16];

    public BunkerAsyncChunk(AsyncWorld world, int cx, int cz) {
        super(null, new ChunkLocation(world, cx, cz));
        this.cx = cx;
        this.cz = cz;
        Arrays.fill(biomes, (byte) 1);
    }

    public synchronized Chunk getNmsStoredChunk() {
        return this.nmsStoredChunk;
    }

    public int getX() {
        return this.cx;
    }

    public int getZ() {
        return this.cz;
    }

    @Override
    public synchronized void writeBlock(int x, int y, int z, int id, byte data) {
        if (id == -1)
            return;
        writeBlock(x, y, z, data << 12 | id, true);
    }

    @Override
    public synchronized void setEmittedLight(int x, int y, int z, int value) {
        int sectionIndex = y >> 4;
        ChunkSection section = sections[sectionIndex];
        if (section == null) {
            section = sections[sectionIndex] = new ChunkSection(sectionIndex << 4, true);
        }
        section.getEmittedLightArray().a(x, y & 0xF, z, value & 0xF);
    }

    @Override
    public synchronized void writeBlock(int section, int index, int combinedBlockId, boolean addTile) {
        writeBlock(getLX(index), getLY(index) + (section << 4), getLZ(index), combinedBlockId, addTile);
    }

    public synchronized void writeBlock(int x, int y, int z, int block, boolean addTile) {
        int sectionIndex = y >> 4;
        ChunkSection section = sections[sectionIndex];
        if (section == null) {
            section = sections[sectionIndex] = new ChunkSection(sectionIndex << 4, true);
        }
        section.getSkyLightArray().a(x, y & 15, z, 15);
        section.getBlocks().setBlock(x, y & 15, z, Block.getByCombinedId(block));
        if (addTile && hasTileEntity(block & 0xFFF)) {
            setTileEntity(x, y, z, new TagCompound());
        }
    }

    public synchronized void setBiome(int biome) {
        Arrays.fill(biomes, (byte) biome);
        this.flushBiomes();
    }

    public synchronized void setBiome(int x, int z, int biome) {
        this.biomes[z << 4 | x] = (byte) (biome & 0xFF);
    }

    public synchronized void flushBiomes() {
        nmsStoredChunk.a(biomes);
    }

    @Override
    public short getCombinedBlockSync(int x, int y, int z) {
        return 0;
    }

    @Override
    protected void optimizeSection(int i, GUChunkSection guChunkSection) {

    }

    @Override
    public void start() {
    }

    @Override
    public void end(int mask) {
    }

    public synchronized void waitForFinish() {
        if (!this.finished) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void finish(BunkerWorldServer server) {
        nmsStoredChunk = new Chunk(server, this.getX(), this.getZ());
        Chunk nmsChunk = nmsStoredChunk;
        nmsChunk.mustSave = true;
        nmsChunk.f(true);

        try {

            for (ChunkSection section : sections) {
                if (section == null)
                    continue;
                section.recalcBlockCounts();
            }

            nmsChunk.a(sections); //Set the blocks

            //Tile Entities
            getTiles().forEach((intVector3D, te) -> {
                BlockPosition bp = new BlockPosition(intVector3D.getX(), intVector3D.getY(), intVector3D.getZ());
                TileEntity entity = nmsChunk.a(bp, Chunk.EnumTileEntityState.IMMEDIATE); //Get or Create tile entity or null if none is applicable to the block at that position
                if (entity != null) {
                    //Set Tile Entity's Coordinates in it's NBT
                    te.getData().put("x", new TagInt(bp.getX()));
                    te.getData().put("y", new TagInt(bp.getY()));
                    te.getData().put("z", new TagInt(bp.getZ()));

                    entity.load(fromGenericCompound(te)); //Load NBT into tile entity
                }
            });

            this.flushBiomes();
            nmsChunk.initLighting();
        } finally {
            finished = true;
            this.notifyAll();
        }
    }

    @Override
    protected void update() {
    }

    public void loadFromChunk(Chunk chunk) {
        ChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            ChunkSection section = sections[sectionIndex];
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int block = section != null ? Block.getCombinedId(sections[sectionIndex].getType(x, y, z)) : 0;
                        this.writeBlock(x, y + (sectionIndex << 4), z, block & 4095, (byte) (block >>> 12));
                    }
                }
            }
        }
    }

    @Override
    protected void loadFromChunk(int sectionMask) {
    }

    private NBTTagCompound fromGenericCompound(TagCompound compound) {
        return (NBTTagCompound) fromGenericTag(compound);
    }

    private NBTBase fromGenericTag(Tag tag) {
        if (tag instanceof TagCompound) {
            NBTTagCompound compound = new NBTTagCompound();
            Map<String, Tag> tags = ((TagCompound) tag).getData();
            tags.forEach((k, t) -> compound.set(k, fromGenericTag(t)));
            return compound;
        } else if (tag instanceof TagShort) {
            return new NBTTagShort(((TagShort) tag).getData());
        } else if (tag instanceof TagLong) {
            return new NBTTagLong(((TagLong) tag).getData());
        } else if (tag instanceof TagLongArray) {
            return new NBTTagLongArray(((TagLongArray) tag).getData());
        } else if (tag instanceof TagInt) {
            return new NBTTagInt(((TagInt) tag).getData());
        } else if (tag instanceof TagByte) {
            return new NBTTagByte(((TagByte) tag).getData());
        } else if (tag instanceof TagByteArray) {
            return new NBTTagByteArray(((TagByteArray) tag).getData());
        } else if (tag instanceof TagString) {
            return new NBTTagString(((TagString) tag).getData());
        } else if (tag instanceof TagList) {
            NBTTagList list = new NBTTagList();
            ((TagList) tag).getData().forEach(t -> list.add(fromGenericTag(t)));
            return list;
        } else if (tag instanceof TagIntArray) {
            return new NBTTagIntArray(((TagIntArray) tag).getData());
        } else if (tag instanceof TagFloat) {
            return new NBTTagFloat(((TagFloat) tag).getData());
        } else if (tag instanceof TagDouble) {
            return new NBTTagDouble(((TagDouble) tag).getData());
        }
        throw new IllegalArgumentException("Tag is not of a recognized type (" + tag.getClass().getName() + ")");
    }
}