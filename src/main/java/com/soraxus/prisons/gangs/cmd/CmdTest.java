package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.Synchronizer;
import net.ultragrav.asyncworld.customworld.CustomWorld;
import net.ultragrav.asyncworld.customworld.SpigotCustomWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class CmdTest extends GangCommand {
    public CmdTest() {
        this.addAlias("ctest");
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            long ms = System.currentTimeMillis();
            Bukkit.broadcastMessage("Instantiating");
            CustomWorld world = new SpigotCustomWorld(SpigotPrisonCore.instance, UUID.randomUUID().toString(), 3, 3);
            Bukkit.broadcastMessage("Creating...");
            world.create((a) -> a.setBlock(5, 80, 5, Material.BEDROCK.getId(), (byte) 0));
            Bukkit.broadcastMessage("Teleport...");
            Location loc = new Location(world.getBukkitWorld(), 5, 81, 5);
            ms = System.currentTimeMillis() - ms;
            Bukkit.broadcastMessage("Created in " + ms + "ms");
            Synchronizer.synchronize(() -> getSpigotPlayer().teleport(loc));
        });
    }
}
