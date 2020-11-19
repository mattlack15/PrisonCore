package com.soraxus.prisons.privatemines;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.asyncworld.customworld.SpigotCustomWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class PrivateMine extends Mine implements GravSerializable {
    private final Gang gang;
    private SpigotCustomWorld customWorld;
    private MineVisitationManager visitationManager = new MineVisitationManager(this);
    private Meta meta = new Meta();
    private int i = 0;

    public PrivateMine(Gang gang) {
        super(gang.getName(), null);
        this.customWorld = new SpigotCustomWorld(SpigotPrisonCore.instance, "privatemine::" + gang.getId().toString().replace("-", ""), 5, 5);
        this.gang = gang;
        this.setRank(this.getRank());
        EventSubscriptions.instance.subscribe(this);
    }

    public static PrivateMine deserialize(GravSerializer serializer, Gang gang) {
        PrivateMine mine = new PrivateMine(gang);
        mine.visitationManager = MineVisitationManager.deserialize(serializer, mine);
        mine.meta = new Meta(serializer);
        mine.setRank(mine.getRank());
        return mine;
    }

    public void teleport(Player player) {
        player.teleport(new Location(this.customWorld.getBukkitWorld(), 2.0D, 83.1D, 2.0D));
    }

    public World getWorld() {
        return this.customWorld.isWorldCreated() ? this.customWorld.getBukkitWorld() : null;
    }

    public void tick() {
        if (this.i++ == 10) {
            this.i = 0;
            this.customWorld.getBukkitWorld().getPlayers().forEach((p) -> {
                if (!p.hasPermission("privatemines.bypass")) {
                    MineVisitor visitor = this.getVisitationManager().getVisitor(p.getUniqueId());
                    if (visitor == null) {
                        if (!this.gang.isMember(p.getUniqueId())) {
                            p.sendMessage("§cYou are not allowed in this private mine!");
                            p.teleport(SpigotPrisonCore.instance.getSpawn());
                        }

                    } else if (!visitor.getVisitationType().equals(VisitationType.FREE)) {
                        AtomicInteger session = visitor.getCurrentSessionTicks();
                        int sessionTicks;
                        if ((sessionTicks = session.addAndGet(10)) >= 1200) {
                            Economy moneyEco = Economy.money;
                            long price = (long) ((double) this.getVisitationManager().getRentalPrice() * ((double) sessionTicks / 60.0D));
                            if (!moneyEco.hasBalance(p.getUniqueId(), price)) {
                                p.sendMessage("§cYou are no longer able to pay the rent for this private mine!");
                                p.teleport(SpigotPrisonCore.instance.getSpawn());
                            } else {
                                moneyEco.removeBalance(p.getUniqueId(), price);
                                this.gang.addBalance(price);
                                session.set(0);
                            }
                        }

                    }
                }
            });
        }

    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        if(!this.customWorld.isWorldCreated())
            return;
        if(!event.getBlock().getWorld().equals(this.customWorld.getBukkitWorld()))
            return;
        if(getRegion().contains(Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector())))
            return;
        event.setCancelled(true);
    }

    @EventSubscription
    private void onTeleport(PlayerTeleportEvent event) {
        if (this.customWorld.isWorldCreated()) {
            if (event.getTo().getWorld().equals(this.customWorld.getBukkitWorld())) {
                Player p = event.getPlayer();
                if (p.hasPermission("privatemines.bypass")) {
                    return;
                }

                MineVisitor visitor = this.getVisitationManager().getVisitor(p.getUniqueId());
                if (visitor == null) {
                    VisitationType result = getVisitationManager().tryAddVisitor(p.getUniqueId());
                    if (result == null) {
                        p.sendMessage("§cSorry, there is no more room in this mine for you :(");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getLocation().getWorld().equals(this.getWorld())) {
            event.getPlayer().teleport(SpigotPrisonCore.instance.getSpawn());
        }

    }

    @EventSubscription
    private void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld().equals(this.getWorld())) {
            event.setCancelled(true);
        }

    }

    public boolean shouldSave() {
        return false;
    }

    public synchronized int getRank() {
        return (Integer) this.meta.getOrSet("rank", 1);
    }

    public synchronized boolean upgrade() {
        return this.setRank(this.getRank() + 1);
    }

    public synchronized boolean setRank(int rank) {
        if (MineManager.instance.getLoaded().size() <= rank - 1) {
            return false;
        } else {
            this.clearBlocks();
            ((Mine) MineManager.instance.getLoaded().get(rank - 1)).getBlocks().forEach(this::addMineBlock);
            this.meta.set("rank", rank);
            return true;
        }
    }

    public void serialize(GravSerializer serializer) {
        serializer.writeObject(this.getBlocks());
        this.visitationManager.serialize(serializer);
        this.meta.serialize(serializer);
    }

    public void create(Schematic mineSchematic, CuboidRegion mineRegion) {
        this.customWorld.create((w) -> {
            long ms = System.currentTimeMillis();
            w.pasteSchematic(mineSchematic, new IntVector3D(0, 0, 0), true);
            ms = System.currentTimeMillis() - ms;
            System.out.println("Generated world in " + ms + "ms");
        });
        this.customWorld.getBukkitWorld().setSpawnFlags(false, false);
        this.customWorld.getBukkitWorld().setGameRuleValue("doWeatherCycle", "false");
        this.customWorld.getBukkitWorld().setPVP(false);
        this.setRegion(new CuboidRegion(this.customWorld.getBukkitWorld(), mineRegion.getMinimumPoint(), mineRegion.getMaximumPoint()));
        this.reset();
    }

    public void unloadWorld() {
        this.customWorld.getBukkitWorld().getPlayers().forEach((p) -> p.teleport(SpigotPrisonCore.instance.getSpawn()));
        this.customWorld.unload();
    }

    public Gang getGang() {
        return this.gang;
    }

    public MineVisitationManager getVisitationManager() {
        return this.visitationManager;
    }
}