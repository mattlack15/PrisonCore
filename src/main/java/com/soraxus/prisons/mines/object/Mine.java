package com.soraxus.prisons.mines.object;

import com.soraxus.prisons.bunkers.util.BHoloTextBox;
import com.soraxus.prisons.mines.ModuleMines;
import com.soraxus.prisons.mines.manager.MineManager;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Mine {
    private static final String REGION = "region";
    private static final String BLOCKS = "blocks";
    private CuboidRegion region;
    private String name;
    private Map<Integer, Double> blocks = new ConcurrentHashMap<>();
    private int blocksMined = 0;
    private volatile boolean resetting = false;
    private long futureCreatedAt = -1L;
    private volatile CompletableFuture<Void> resetFuture = null;
    private String permission = null;
    private final AtomicLong lastMinedBlock = new AtomicLong();
    private int order = 0;
    private BHoloTextBox textBox = null;
    private final ReentrantLock ioLock = new ReentrantLock();

    public static void renameMine(Mine mine, MineManager manager, String name) {
        manager.remove(mine.getName());
        mine.name = name;
        manager.add(mine);
    }

    public Mine(String name, CuboidRegion region) {
        this.name = name;
        this.region = region;
    }

    public synchronized boolean isResetting() {
        if (this.resetting && System.currentTimeMillis() - this.futureCreatedAt > 1000L) {
            this.reset();
        }

        return this.resetting;
    }

    public void updateArmorStand() {
    }

    public boolean shouldSave() {
        return true;
    }

    public static Mine fromConfigSection(ConfigurationSection section) {
        String name = section.getName();
        Vector3D point1 = Vector3D.fromBukkitVector(section.getVector("region.point1"));
        Vector3D point2 = Vector3D.fromBukkitVector(section.getVector("region.point2"));
        World world = Bukkit.getWorld(section.getString("world"));
        if (world == null) {
            throw new IllegalStateException("Invalid world encountered while loading mine named: " + name);
        } else {
            CuboidRegion region = new CuboidRegion(world, point1, point2);
            Map<Integer, Double> chances = new ConcurrentHashMap<>();
            ConfigurationSection blockSection = section.getConfigurationSection("blocks");
            Iterator var8 = blockSection.getKeys(false).iterator();

            int blocksMined;
            while(var8.hasNext()) {
                String keys = (String)var8.next();
                blocksMined = blockSection.getInt(concatForConfig(keys, "id"));
                byte data = (byte)blockSection.getInt(concatForConfig(keys, "data"));
                double chance = blockSection.getDouble(concatForConfig(keys, "chance"));
                chances.put(data << 12 | blocksMined, chance);
            }

            long lastMined = section.getLong("last-block-mined", System.currentTimeMillis());
            blocksMined = section.getInt("blocks-mined");
            String permission = section.getString("permission");
            int order = section.getInt("order", 0);
            Mine mine = new Mine(name, region);
            mine.setBlocksMined(blocksMined);
            mine.setPermission(permission);
            mine.setOrder(order);
            mine.getLastMinedBlock().set(lastMined);
            mine.blocks = chances;
            return mine;
        }
    }

    public Map<Integer, Double> getBlocks() {
        return new HashMap<>(this.blocks);
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    public static String concatForConfig(String... strings) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < strings.length; ++i) {
            String str = strings[i];
            builder.append(str);
            if (i != strings.length - 1) {
                builder.append(".");
            }
        }

        return builder.toString();
    }

    public void addMineBlock(int combinedId, double chance) {
        this.blocks.put(combinedId, chance);
    }

    public void removeMineBlock(int combinedId) {
        this.blocks.remove(combinedId);
    }

    public synchronized void incrementBlocksMined(int amount) {
        this.setBlocksMined(this.getBlocksMined() + amount);
    }

    public synchronized void setBlocksMined(int blocksMined) {
        this.blocksMined = blocksMined;
        if ((double)blocksMined / (double)this.getMineArea() >= ModuleMines.instance.getMinedThreshold()) {
            this.reset();
        }

    }

    public int getMineArea() {
        return this.region.getArea();
    }

    public synchronized CompletableFuture<Void> clear() {
        if (!this.isResetting()) {
            this.resetting = true;
            this.blocksMined = this.getMineArea();
            AsyncWorld world = new SpigotAsyncWorld(this.region.getWorld());
            world.setBlocks(this.region, () -> (short) 0);
            CompletableFuture<Void> future = world.flush();
            this.resetFuture = future.thenAccept((a) -> {
                this.resetting = false;
            });
        }
        return this.resetFuture;
    }

    public synchronized CompletableFuture<Void> reset() {
        if (this.resetting && System.currentTimeMillis() - this.futureCreatedAt <= 1000L) {
            return this.resetFuture;
        } else {
            this.resetting = true;
            Bukkit.getOnlinePlayers().stream().filter((p) -> p.getWorld().equals(this.region.getWorld())).filter((p) -> this.region.contains(new Vector3D(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))).forEach((p) -> {
                Location loc = p.getLocation();
                loc.setY((double)this.region.getMaximumY() + 1.5D);
                p.teleport(loc, TeleportCause.PLUGIN);
                p.sendMessage(ModuleMines.instance.getPrefix() + "This mine is resetting! Saving your butt...");
            });
            Random rand = new Random(System.currentTimeMillis());
            Map<Integer, Double> chances = new HashMap<>(this.getBlocks());
            double totalChances = 0.0D;

            Double value;
            for(Iterator<?> var5 = chances.values().iterator(); var5.hasNext(); totalChances += value) {
                value = (Double)var5.next();
            }

            AsyncWorld world = new SpigotAsyncWorld(this.region.getWorld());
            double finalTotalChances = totalChances;
            world.setBlocks(this.region, () -> {
                double randNum = rand.nextDouble() * finalTotalChances;
                double counter = 0.0D;
                Iterator<Entry<Integer, Double>> var8 = chances.entrySet().iterator();

                Entry<Integer, Double> entry;
                do {
                    if (!var8.hasNext()) {
                        return (short) 0;
                    }

                    entry = var8.next();
                    counter += entry.getValue();
                } while(randNum > counter);

                return (short) (int) entry.getKey();
            });
            CompletableFuture<Void> future = world.flush();
            this.futureCreatedAt = System.currentTimeMillis();
            this.resetFuture = future.thenAccept((a) -> {
                synchronized(this) {
                    this.resetting = false;
                }
            });
            this.blocksMined = 0;
            return this.resetFuture;
        }
    }

    public void saveToConfigSection(ConfigurationSection section) {
        if (this.shouldSave()) {
            this.ioLock.lock();
            section.set(concatForConfig("region", "point1"), this.region.getMinimumPoint().toBukkitVector());
            section.set(concatForConfig("region", "point2"), this.region.getMaximumPoint().toBukkitVector());
            section.set("world", this.region.getWorld().getName());
            int i = 0;
            section.createSection("blocks");

            for(Iterator var3 = this.blocks.entrySet().iterator(); var3.hasNext(); ++i) {
                Entry<Integer, Double> entry = (Entry)var3.next();
                int id = (Integer)entry.getKey() & 4095;
                byte data = (byte)((Integer)entry.getKey() >> 12);
                double chance = (Double)entry.getValue();
                section.getConfigurationSection("blocks").set(concatForConfig(Integer.toString(i), "id"), id);
                section.getConfigurationSection("blocks").set(concatForConfig(Integer.toString(i), "data"), data);
                section.getConfigurationSection("blocks").set(concatForConfig(Integer.toString(i), "chance"), chance);
            }

            section.set("last-block-mined", this.lastMinedBlock.get());
            section.set("blocks-mined", this.blocksMined);
            section.set("permission", this.permission);
            section.set("order", this.order);
            this.ioLock.unlock();
        }
    }

    public CuboidRegion getRegion() {
        return this.region;
    }

    public void setRegion(CuboidRegion region) {
        this.region = region;
    }

    public String getName() {
        return this.name;
    }

    public int getBlocksMined() {
        return this.blocksMined;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public AtomicLong getLastMinedBlock() {
        return this.lastMinedBlock;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public BHoloTextBox getTextBox() {
        return this.textBox;
    }

    public ReentrantLock getIoLock() {
        return this.ioLock;
    }
}