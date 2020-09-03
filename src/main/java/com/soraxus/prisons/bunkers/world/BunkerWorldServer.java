package com.soraxus.prisons.bunkers.world;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;


public class BunkerWorldServer extends WorldServer {
    @Getter
    @Setter
    private boolean ready = false;

    BunkerWorldServer(@NotNull BunkerWorldDataManager dataManager, int dimension) {
        super(
                MinecraftServer.getServer(),
                dataManager,
                dataManager.getWorldData(),
                dimension,
                MinecraftServer.getServer().methodProfiler,
                World.Environment.NORMAL,
                new BunkerChunkGenerator()
        );
        this.keepSpawnInMemory = false;
        this.D = new CustomFunctionData(null, MinecraftServer.getServer());
        this.tracker = new EntityTracker(this);
        addIWorldAccess(new WorldManager(MinecraftServer.getServer(), this));

        worldData.setDifficulty(EnumDifficulty.NORMAL);
        worldData.setSpawn(new BlockPosition(0, 61, 0));
        super.setSpawnFlags(false, false); // allowMonsters, allowAnimals

        this.getWorld().setGameRuleValue("randomTickSpeed", "0");

        this.pvpMode = false;
    }

    @Override
    public void save(boolean forceSave, IProgressUpdate progressUpdate) throws ExceptionWorldConflict {
        super.save(forceSave, progressUpdate); //possibly remove
    }

    private void save() {

    }
}