package com.soraxus.prisons.mines.object;

import com.soraxus.prisons.mines.ModuleMines;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class Mine {
    //Config Sections
    private static final String REGION = "region";
    private static final String BLOCKS = "blocks";

    @Getter
    private CuboidRegion region;
    @Getter
    private String name;
    private Map<Integer, Double> blocks = new HashMap<>();
    @Getter
    private int blocksMined = 0;
    private volatile boolean resetting = false;
    private long futureCreatedAt = -1;
    private volatile CompletableFuture<Void> resetFuture = null;
    @Getter
    @Setter
    private String permission = null;
    @Getter
    private ReentrantLock ioLock = new ReentrantLock();

    public Mine(String name, CuboidRegion region) {
        this.name = name;
        this.region = region;
    }

    public synchronized boolean isResetting() {
        if(this.resetting) {
            if(System.currentTimeMillis() - futureCreatedAt > 1000) {
                reset();
            }
        }
        return this.resetting;
    }

    /**
     * Creates a mine from the specified section assuming the section was previously saved to by a Mine instance
     */
    public static Mine fromConfigSection(ConfigurationSection section) {

        //Region
        String name = section.getName();
        Vector3D point1 = Vector3D.fromBukkitVector(section.getVector("region.point1"));
        Vector3D point2 = Vector3D.fromBukkitVector(section.getVector("region.point2"));
        World world = Bukkit.getWorld(section.getString("world"));

        if (world == null)
            throw new IllegalStateException("Invalid world encountered while loading mine named: " + name);


        CuboidRegion region = new CuboidRegion(world, point1, point2);

        //Blocks
        Map<Integer, Double> chances = new HashMap<>();
        ConfigurationSection blockSection = section.getConfigurationSection(BLOCKS);
        for (String keys : blockSection.getKeys(false)) {
            int id = blockSection.getInt(concatForConfig(keys, "id"));
            byte data = (byte) blockSection.getInt(concatForConfig(keys, "data"));
            double chance = blockSection.getDouble(concatForConfig(keys, "chance"));
            chances.put(data << 12 | id, chance);
        }

        //Blocks Mined
        int blocksMined = section.getInt("blocks-mined");

        //Permission
        String permission = section.getString("permission");

        Mine mine = new Mine(name, region);
        mine.setBlocksMined(blocksMined);
        mine.setPermission(permission);
        mine.blocks = chances;
        return mine;
    }

    public Map<Integer, Double> getBlocks() {
        return new HashMap<>(blocks);
    }

    public static String concatForConfig(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            String str = strings[i];
            builder.append(str);
            if (i != strings.length - 1)
                builder.append(".");
        }
        return builder.toString();
    }

    public void addMineBlock(int combinedId, double chance) {
        blocks.put(combinedId, chance);
    }

    public void removeMineBlock(int combinedId) {
        blocks.remove(combinedId);
    }

    public void incrementBlocksMined(int amount) {
        this.setBlocksMined(this.getBlocksMined() + amount);
    }

    public void setBlocksMined(int blocksMined) {
        this.blocksMined = blocksMined;
        if (blocksMined / (double) getMineArea() >= ModuleMines.instance.getMinedThreshold())
            this.reset();
    }

    /**
     * Get the mine-able area within the mine
     */
    public int getMineArea() {
        return region.getArea();
    }

    /**
     * Clears out the mine
     */
    public synchronized CompletableFuture<Void> clear() {
        if (this.isResetting())
            return resetFuture;
        resetting = true;
        this.blocksMined = this.getMineArea();
        AsyncWorld world = new SpigotAsyncWorld(region.getWorld());
        world.setBlocks(region, () -> (short) 0);
        CompletableFuture<Void> future = world.flush();
        resetFuture = future.thenAccept((a) -> resetting = false);
        return resetFuture;
    }

    /**
     * Reset the mine asynchronously
     */
    public synchronized CompletableFuture<Void> reset() {
        if (resetting) { //Don't change to isResetting()
            if(System.currentTimeMillis() - futureCreatedAt <= 1000) {
                return resetFuture;
            }
        }
        resetting = true;

        //Teleport players
        Bukkit.getOnlinePlayers().stream().filter(p -> region.contains(new Vector3D(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))).forEach(p -> {
            Location loc = p.getLocation();
            loc.setY(region.getMaximumY() + 1.5);
            p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            p.sendMessage(ModuleMines.instance.getPrefix() + "This mine is resetting! Saving your butt...");
        });

        //Actually reset
        Random rand = new Random(System.currentTimeMillis());
        Map<Integer, Double> chances = new HashMap<>(getBlocks());

        double totalChances = 0;
        for (Double value : chances.values()) {
            totalChances += value;
        }

        AsyncWorld world = new SpigotAsyncWorld(region.getWorld());
        double finalTotalChances = totalChances;

        world.setBlocks(region, () -> {
            double randNum = rand.nextDouble() * finalTotalChances;
            double counter = 0;
            for (Map.Entry<Integer, Double> entry : chances.entrySet()) {
                counter += entry.getValue();
                if (randNum <= counter) {
                    return (short) (int) entry.getKey();
                }
            }
            return (short) 0;
        });
        CompletableFuture<Void> future = world.flush();
        futureCreatedAt = System.currentTimeMillis();
        resetFuture = future.thenAccept((a) -> {
            synchronized (this) {
                resetting = false;
                blocksMined = 0;
            }
        });
        return resetFuture;
    }

    public void saveToConfigSection(ConfigurationSection section) {
        ioLock.lock();
        //Region
        section.set(concatForConfig(REGION, "point1"), region.getMinimumPoint().toBukkitVector());
        section.set(concatForConfig(REGION, "point2"), region.getMaximumPoint().toBukkitVector());
        section.set("world", region.getWorld().getName());

        //Blocks
        int i = 0;
        section.createSection(BLOCKS);
        for (Map.Entry<Integer, Double> entry : blocks.entrySet()) {
            int id = entry.getKey() & 4095;
            byte data = (byte) (entry.getKey() >> 12);
            double chance = entry.getValue();

            section.getConfigurationSection(BLOCKS).set(concatForConfig(Integer.toString(i), "id"), id);
            section.getConfigurationSection(BLOCKS).set(concatForConfig(Integer.toString(i), "data"), data);
            section.getConfigurationSection(BLOCKS).set(concatForConfig(Integer.toString(i), "chance"), chance);

            i++;
        }

        //Blocks Mined
        section.set("blocks-mined", blocksMined);

        //Permission
        section.set("permission", permission);
        ioLock.unlock();
    }
}
