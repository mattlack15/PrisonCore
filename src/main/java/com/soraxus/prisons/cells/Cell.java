package com.soraxus.prisons.cells;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.cells.minions.Minion;
import com.soraxus.prisons.cells.minions.MinionManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import net.ultragrav.asyncworld.customworld.CustomWorld;
import net.ultragrav.asyncworld.customworld.CustomWorldAsyncWorld;
import net.ultragrav.asyncworld.customworld.SavedCustomWorld;
import net.ultragrav.asyncworld.customworld.SpigotCustomWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class Cell implements GravSerializable {
    private final UUID player;
    private final CustomWorld world;
    private Meta meta = new Meta();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final CellSettings settings = new CellSettings(this);
    private final MinionManager minionManager;
    private final int sizeChunksX = 4;
    private final int sizeChunksZ = 4;
    private final Map<UUID, Location> prevLocs = new HashMap<>();

    public Cell(UUID player) {
        this.player = this.meta.getOrSet("playerId", player);
        this.world = new SpigotCustomWorld(SpigotPrisonCore.instance, "cell_" + player.toString().replace("-", ""), 4, 4);
        this.minionManager = new MinionManager(this);
    }

    public Cell(GravSerializer serializer) {
        this.meta = new Meta(serializer);
        this.player = this.meta.get("playerId");
        this.world = new SpigotCustomWorld(SpigotPrisonCore.instance, "cell_" + player.toString().replace("-", ""), 4, 4);
        this.minionManager = new MinionManager(this);
    }

    public void update() {
        this.minionManager.update();
    }

    public UUID getId() {
        return this.player;
    }

    public void generateWorld(Schematic cellSchematic) {
        long ms = System.currentTimeMillis();
        SavedCustomWorld world = this.meta.get("world");
        if (world != null) {
            this.world.create(world, false);
        } else {
            this.world.create((w) -> {
                if(cellSchematic != null) {
                    int targetY = 60;
                    IntVector3D dimensions = cellSchematic.getDimensions();
                    int halfX = dimensions.getX() / 2;
                    int halfZ = dimensions.getZ() / 2;
                    int placeX = (4 << 3) - halfX;
                    int placeZ = (4 << 3) - halfZ;
                    w.pasteSchematic(cellSchematic, new IntVector3D(placeX, targetY, placeZ), true);
                } else {
                    w.setBlocks(new CuboidRegion(null, new Vector3D(0, 0, 0),
                                    new Vector3D(16*4-1, 60, 16*4-1)),
                            () -> (short)1);
                }
            });
        }

        this.world.getBukkitWorld().getWorldBorder().setCenter(32.0D, 32.0D);
        this.world.getBukkitWorld().getWorldBorder().setSize(getSettings().getShowBorder() ? 64.0D : 10000.0D);
        this.world.getBukkitWorld().getWorldBorder().setWarningDistance(0);
        this.world.getBukkitWorld().setPVP(false);
        this.world.getBukkitWorld().setGameRuleValue("doMobSpawning", "false");
        this.world.getBukkitWorld().setGameRuleValue("doDaylightCycle", "false");
        this.world.getBukkitWorld().setGameRuleValue("doWeatherCycle", "false");
        this.world.getBukkitWorld().setThundering(false);
        this.world.getBukkitWorld().setStorm(false);
        this.world.getBukkitWorld().setTime((Long) this.meta.getOrSet("worldTime", 0L));
        Synchronizer.synchronize(() -> {
            if (this.isWorldCreated()) {
                this.minionManager.spawnAll();
            }

        });
        this.meta.set("world", (Object) null);
        EventSubscriptions.instance.subscribe(this);
        ms = System.currentTimeMillis() - ms;
    }

    public void setSettingWorldTime(int time) {
        this.world.getBukkitWorld().setTime((long) time);
        this.getSettings().setWorldTime(time);
    }

    public int getSettingWorldTime() {
        return this.getSettings().getWorldTime();
    }

    public List<TrustedPlayer> getTrustedPlayers() {
        this.lock.lock();

        ArrayList var1;
        try {
            var1 = new ArrayList<>(this.meta.getOrSet("trusted", new ArrayList<>()));
        } finally {
            this.lock.unlock();
        }

        return var1;
    }

    public void removeTrustedPlayer(TrustedPlayer player) {
        this.lock.lock();

        try {
            this.meta.getOrSet("trusted", new ArrayList<>()).remove(player);
        } finally {
            this.lock.unlock();
        }

    }

    public void addTrustedPlayer(TrustedPlayer player) {
        this.lock.lock();

        try {
            List<TrustedPlayer> trusted = this.meta.getOrSet("trusted", new ArrayList<>());
            if (!trusted.contains(player)) {
                trusted.add(player);
            }
        } finally {
            this.lock.unlock();
        }

    }

    public boolean isWorldCreated() {
        return this.world.getBukkitWorld() != null;
    }

    public synchronized void saveAndUnloadWorld() {
        if (this.isWorldCreated()) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            CustomWorldAsyncWorld worldAsyncWorld = this.world.getAsyncWorld();
            Synchronizer.synchronize(() -> {
                this.minionManager.destroyAll();
                this.world.getBukkitWorld().getPlayers().forEach((p) -> {
                    if (this.prevLocs.containsKey(p.getUniqueId())) {
                        p.teleport(this.prevLocs.get(p.getUniqueId()));
                    } else {
                        p.teleport(new Location(Bukkit.getWorld("world"), 0.0D, 80.0D, 0.0D));
                    }

                });
                this.world.unload();
                f.complete(null);
            });
            f.join();
            this.meta.set("world", this.world.getSavedCustomWorld(worldAsyncWorld));
            this.minionManager.save(this.meta);
            EventSubscriptions.instance.unSubscribeAll(this);
        }
    }

    public CuboidRegion getBoundingRegion() {
        return new CuboidRegion(this.world.getBukkitWorld(), new Vector3D(0, 0, 0), new Vector3D(63, 160, 63));
    }

    public void serialize(GravSerializer gravSerializer) {
        this.meta.serialize(gravSerializer);
    }

    @EventSubscription
    private void onDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity().getLocation().getWorld().equals(this.world.getBukkitWorld()) && entityDamageEvent.getEntity().getType() == EntityType.PLAYER) {
            entityDamageEvent.setCancelled(true);
        }

    }

    @EventSubscription
    private void onInteract(PlayerInteractEvent event) {
        if (!ModuleCells.instance.getBypassMode(event.getPlayer().getUniqueId())) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                if (this.getSettings().getProtectionSetting() != CellSettings.ProtectionSetting.EVERYONE) {
                    if (event.getPlayer().getWorld().equals(this.world.getBukkitWorld()) && (!this.isTrusted(event.getPlayer().getUniqueId()) || this.getSettings().getProtectionSetting() != CellSettings.ProtectionSetting.TRUSTED) && !this.getPlayer().equals(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        Iterator<Minion> var2 = this.getMinionManager().getMinions().iterator();

        Minion minion;
        do {
            if (!var2.hasNext()) {
                return;
            }

            minion = var2.next();
        } while (!minion.getMiningBlockLocation().toBukkitVector().equals(event.getBlock().getLocation().toVector()));

        event.setCancelled(true);
    }

    public boolean isTrusted(UUID id) {
        Iterator<TrustedPlayer> var2 = this.getTrustedPlayers().iterator();

        TrustedPlayer trustedPlayer;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            trustedPlayer = var2.next();
        } while (!trustedPlayer.getId().equals(id));

        return true;
    }

    @EventSubscription
    private void onBuild(BlockPlaceEvent event) {
        if (!ModuleCells.instance.getBypassMode(event.getPlayer().getUniqueId())) {
            if (this.getSettings().getProtectionSetting() != CellSettings.ProtectionSetting.EVERYONE) {
                if (event.getBlock().getLocation().getWorld().equals(this.world.getBukkitWorld()) && (!this.isTrusted(event.getPlayer().getUniqueId()) || this.getSettings().getProtectionSetting() != CellSettings.ProtectionSetting.TRUSTED) && !this.getPlayer().equals(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }

            }
        }
    }

    @EventSubscription
    private void onTp(PlayerTeleportEvent event) {
        if (event.getTo().getWorld() == this.world.getBukkitWorld() && event.getFrom().getWorld() != this.world.getBukkitWorld()) {
            this.prevLocs.put(event.getPlayer().getUniqueId(), event.getFrom());
        }

    }

    public UUID getPlayer() {
        return this.player;
    }

    public CustomWorld getWorld() {
        return this.world;
    }

    public Meta getMeta() {
        return this.meta;
    }

    public CellSettings getSettings() {
        return this.settings;
    }

    public MinionManager getMinionManager() {
        return this.minionManager;
    }
}