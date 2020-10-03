package com.soraxus.prisons.bunkers.world;

import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkRegionLoader;
import net.minecraft.server.v1_12_R1.World;

//@RequiredArgsConstructor
public class BunkerChunkLoader extends ChunkRegionLoader {
    private final BunkerWorld bunkerWorld;

    public BunkerChunkLoader(BunkerWorld world) {
        super(null, null);
        this.bunkerWorld = world;
    }

    // Load chunk
    @Override
    public Chunk a(World nmsWorld, int x, int z) {
        Chunk chunk = null;
        if(chunk == null)
            return null;
        chunk.d(true);
        chunk.e(true);
        return chunk;
    }

    @Override
    public void saveChunk(World world, Chunk chunk, boolean unloaded) {
    }

    // Save all chunks
    @Override
    public void c() {
        // All chunks are cached in BunkerWorld, so this is not necessary
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true; // Prevent chunk generator from being called
    }

    // Does literally nothing
    @Override
    public void b() {
    }


    /*private byte[] toByteArray(int[] ints) {
        ByteBuffer buf = ByteBuffer.allocate(ints.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        buf.asIntBuffer().put(ints);

        return buf.array();
    }

    private Entity loadEntity(CompoundTag tag, World world, Chunk chunk) {
        Entity entity = EntityTypes.a((NBTTagCompound) Converter.convertTag(tag), world);
        chunk.g(true);

        if (entity != null) {
            chunk.a(entity);

            CompoundMap map = tag.getValue();

            if (map.containsKey("Passengers")) {
                List<CompoundTag> passengersList = (List<CompoundTag>) map.get("Passengers").getValue();

                for (CompoundTag passengerTag : passengersList) {
                    Entity passenger = loadEntity(passengerTag, world, chunk);

                    if (passengerTag != null) {
                        passenger.a(entity, true);
                    }
                }
            }
        }

        return entity;
    }*/
}
