package com.soraxus.prisons.cells;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.base.Meta;
import com.soraxus.prisons.cells.minions.MinionManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import lombok.Getter;
import net.ultragrav.asyncworld.customworld.CustomWorld;
import net.ultragrav.asyncworld.customworld.CustomWorldAsyncWorld;
import net.ultragrav.asyncworld.customworld.SavedCustomWorld;
import net.ultragrav.asyncworld.customworld.SpigotCustomWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class Cell implements GravSerializable {
    @Getter
    private final UUID player;
    @Getter
    private final CustomWorld world;
    @Getter
    private Meta meta = new Meta();

    private final ReentrantLock lock = new ReentrantLock(true);

    @Getter
    private final CellSettings settings = new CellSettings(this);

    @Getter
    private final MinionManager minionManager;

    private final int sizeChunksX = 4;
    private final int sizeChunksZ = 4;

    public Cell(UUID player) {
        this.player = meta.getOrSet("playerId", player);
        this.world = new SpigotCustomWorld(SpigotPrisonCore.instance, "cell_" + player.toString(), sizeChunksX, sizeChunksZ);
        this.minionManager = new MinionManager(this);
    }

    public Cell(GravSerializer serializer) {
        this.meta = new Meta(serializer);
        this.player = meta.get("playerId");
        this.world = new SpigotCustomWorld(SpigotPrisonCore.instance, "cell_" + player.toString(), sizeChunksX, sizeChunksZ);
        this.minionManager = new MinionManager(this);
    }

    public void update() {
        minionManager.update();
    }

    public UUID getId() {
        return this.player;
    }

    public void generateWorld(Schematic cellSchematic) {
        long ms = System.currentTimeMillis();
        SavedCustomWorld world = meta.get("world");
        if (world != null) {
            this.world.create(world);
        } else if (cellSchematic != null) {
            this.world.create((w) -> {
                int targetY = 60;
                IntVector3D dimensions = cellSchematic.getDimensions();
                int halfX = dimensions.getX() / 2;
                int halfZ = dimensions.getZ() / 2;
                int placeX = (this.sizeChunksX << 3) - halfX; // << 3 just multiplies it by 2^3 (or 8)
                int placeZ = (this.sizeChunksZ << 3) - halfZ;
                w.pasteSchematic(cellSchematic, new IntVector3D(placeX, targetY, placeZ), true);
            });
        }

        this.world.getBukkitWorld().getWorldBorder().setCenter(32, 32);
        this.world.getBukkitWorld().getWorldBorder().setSize(64);
        this.world.getBukkitWorld().getWorldBorder().setWarningDistance(0);

        this.world.getBukkitWorld().setPVP(false);
        this.world.getBukkitWorld().setGameRuleValue("doMobSpawning", "false");
        this.world.getBukkitWorld().setGameRuleValue("doDaylightCycle", "false");
        this.world.getBukkitWorld().setGameRuleValue("doWeatherCycle", "false");
        this.world.getBukkitWorld().setThundering(false);
        this.world.getBukkitWorld().setStorm(false);
        this.world.getBukkitWorld().setTime(meta.getOrSet("worldTime", 0L));

        Synchronizer.synchronize(() -> {
            if(this.isWorldCreated()) {
                minionManager.spawnAll();
            }
        });

        meta.set("world", null); //Free memory
        EventSubscriptions.instance.subscribe(this);
        ms = System.currentTimeMillis() - ms;
        System.out.println("Creating world took: " + ms + "ms");
    }

    public void setSettingWorldTime(int time) {
        this.world.getBukkitWorld().setTime(time);
        getSettings().setWorldTime(time);
    }

    public int getSettingWorldTime() {
        return getSettings().getWorldTime();
    }

    public List<TrustedPlayer> getTrustedPlayers() {
        lock.lock();
        try {
            return new ArrayList<>(this.meta.getOrSet("trusted", new ArrayList<>()));
        } finally {
            lock.unlock();
        }
    }

    public void removeTrustedPlayer(TrustedPlayer player) {
        lock.lock();
        try {
            this.meta.getOrSet("trusted", new ArrayList<>()).remove(player);
        } finally {
            lock.unlock();
        }
    }

    public void addTrustedPlayer(TrustedPlayer player) {
        lock.lock();
        try {
            List<TrustedPlayer> trusted = this.meta.getOrSet("trusted", new ArrayList<>());
            if (!trusted.contains(player))
                trusted.add(player);
        } finally {
            lock.unlock();
        }
    }

    public boolean isWorldCreated() {
        return this.world.getBukkitWorld() != null;
    }

    public void saveAndUnloadWorld() {

        //Unload world
        CompletableFuture<Void> f = new CompletableFuture<>();
        CustomWorldAsyncWorld worldAsyncWorld = this.world.getAsyncWorld();
        Synchronizer.synchronize(() -> {

            this.minionManager.destroyAll();

            this.world.getBukkitWorld().getPlayers().forEach(p -> {
                if (prevLocs.containsKey(p.getUniqueId())) {
                    p.teleport(prevLocs.get(p.getUniqueId()));
                } else {
                    p.teleport(new Location(Bukkit.getWorld("world"), 0, 80, 0));
                }
            });

            this.world.unload();

            f.complete(null);
        });
        f.join();
        this.meta.set("world", this.world.getSavedCustomWorld(worldAsyncWorld));
        this.minionManager.save(this.meta);
        EventSubscriptions.instance.unSubscribe(this);
    }

    public CuboidRegion getBoundingRegion() {
        return new CuboidRegion(this.world.getBukkitWorld(), new Vector3D(0, 0, 0), new Vector3D(63, 160, 63));
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        this.meta.serialize(gravSerializer);
    }

    @EventSubscription
    private void onDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity().getLocation().getWorld().equals(this.world.getBukkitWorld())
        && entityDamageEvent.getEntity().getType() == EntityType.PLAYER)
            entityDamageEvent.setCancelled(true);
    }

    @EventSubscription
    private void onInteract(PlayerInteractEvent event) {
        if(ModuleCells.instance.getBypassMode(event.getPlayer().getUniqueId()))
            return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            return;
        if(getSettings().getProtectionSetting() == CellSettings.ProtectionSetting.EVERYONE)
            return;
        if (event.getPlayer().getWorld().equals(this.world.getBukkitWorld())) {
            if ((!this.isTrusted(event.getPlayer().getUniqueId()) || getSettings().getProtectionSetting() != CellSettings.ProtectionSetting.TRUSTED)
                    && !this.getPlayer().equals(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    public boolean isTrusted(UUID id) {
        for (TrustedPlayer trustedPlayer : getTrustedPlayers())
            if (trustedPlayer.getId().equals(id))
                return true;
        return false;
    }

    @EventSubscription
    private void onBuild(BlockPlaceEvent event) {
        if(ModuleCells.instance.getBypassMode(event.getPlayer().getUniqueId()))
            return;
        if(getSettings().getProtectionSetting() == CellSettings.ProtectionSetting.EVERYONE)
            return;
        if (event.getBlock().getLocation().getWorld().equals(this.world.getBukkitWorld())) {
            if ((!this.isTrusted(event.getPlayer().getUniqueId()) || getSettings().getProtectionSetting() != CellSettings.ProtectionSetting.TRUSTED)
                    && !this.getPlayer().equals(event.getPlayer().getUniqueId()))
                event.setCancelled(true);
        }
    }

    private final Map<UUID, Location> prevLocs = new HashMap<>();

    @EventSubscription
    private void onTp(PlayerTeleportEvent event) {
        if (event.getTo().getWorld() == this.world.getBukkitWorld()) {
            if (event.getFrom().getWorld() != this.world.getBukkitWorld()) {
                this.prevLocs.put(event.getPlayer().getUniqueId(), event.getFrom());
            }
        }
    }
}
