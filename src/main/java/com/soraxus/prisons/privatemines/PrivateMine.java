package com.soraxus.prisons.privatemines;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import lombok.Getter;
import net.ultragrav.asyncworld.customworld.SpigotCustomWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class PrivateMine extends Mine implements GravSerializable {
    @Getter
    private final Gang gang;
    private SpigotCustomWorld customWorld;
    @Getter
    private MineVisitationManager visitationManager = new MineVisitationManager(this);
    private Meta meta = new Meta();
    private int i = 0;
    // TODO: Super constructor

    public PrivateMine(Gang gang) {
        super(gang.getName(), null);
        customWorld = new SpigotCustomWorld(SpigotPrisonCore.instance, "privatemine::" + gang.getId().toString(), 5, 5);
        this.gang = gang;

        //TODO
        this.getBlocks().put(Material.NETHER_BRICK.getId(), 1D);

        EventSubscriptions.instance.subscribe(this);
    }

    public static PrivateMine deserialize(GravSerializer serializer, Gang gang) {
        PrivateMine mine = new PrivateMine(gang);
        mine.getBlocks().putAll(serializer.readObject());
        mine.visitationManager = MineVisitationManager.deserialize(serializer, mine);
        mine.meta = new Meta(serializer);
        return mine;
    }

    public void teleport(Player player) {
        player.teleport(new Location(customWorld.getBukkitWorld(), 2, 83.1, 2));
    }

    public World getWorld() {
        return this.customWorld.isWorldCreated() ? this.customWorld.getBukkitWorld() : null;
    }

    public void tick() {
        if (i++ == 10) { //Every 500ms, collect rent
            i = 0;
            this.customWorld.getBukkitWorld().getPlayers().forEach(p -> {
                if (p.hasPermission("privatemines.bypass")) {
                    return;
                }
                MineVisitor visitor = getVisitationManager().getVisitor(p.getUniqueId());
                if (visitor == null) { // Not a visitor, must be admin or a gang member
                    if (!gang.isMember(p.getUniqueId())) {
                        // Not a gang member, kick them
                        p.sendMessage("§cYou are not allowed in this private mine!");
                        p.teleport(SpigotPrisonCore.instance.getSpawn());
                    }
                    return;
                }

                if (visitor.getVisitationType().equals(VisitationType.FREE))
                    return;

                AtomicInteger session = visitor.getCurrentSessionTicks();
                int sessionTicks;
                if ((sessionTicks = session.addAndGet(10)) >= 20 * 60) {
                    Economy moneyEco = Economy.money;
                    long price = (long) (this.getVisitationManager().getRentalPrice() * (sessionTicks / 60D));
                    if (!moneyEco.hasBalance(p.getUniqueId(), price)) {
                        // Not enough money, kick out
                        p.sendMessage("§cYou are no longer able to pay the rent for this private mine!");
                        p.teleport(SpigotPrisonCore.instance.getSpawn());
                        // Don't set session time, so that next time they try to teleport it instantly takes money
                    } else {
                        // Pay rent
                        moneyEco.removeBalance(p.getUniqueId(), price);
                        gang.addBalance(price);
                        session.set(0);
                    }
                }
            });
        }
    }


    //Prevent non-authorized teleports
    @EventSubscription
    private void onTeleport(PlayerTeleportEvent event) {
        if (!this.customWorld.isWorldCreated())
            return;
        if (event.getTo().getWorld().equals(this.customWorld.getBukkitWorld())) {
            Player p = event.getPlayer();
            if (p.hasPermission("privatemines.bypass")) {
                return;
            }

            MineVisitor visitor = getVisitationManager().getVisitor(p.getUniqueId());
            if (visitor == null) { //Not a visitor, must be admin or a gang member
                if (!gang.isMember(p.getUniqueId())) {
                    if (!getVisitationManager().addVisitor(p.getUniqueId(), VisitationType.RENTAL)) {
                        p.sendMessage("§cYou there is no more room in this private mine!");
                        event.setCancelled(true);
                    }
                } else if (gang.isMember(p.getUniqueId())) {
                    if (!getVisitationManager().addVisitor(p.getUniqueId(), VisitationType.FREE)) {
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

    @Override
    public boolean shouldSave() {
        return false;
    }

    public int getRank() {
        return 1;
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(this.getBlocks());
        visitationManager.serialize(serializer);
        meta.serialize(serializer);
    }

    public void create(Schematic mineSchematic, CuboidRegion mineRegion) {
        customWorld.create((w) -> {
            long ms = System.currentTimeMillis();
            w.pasteSchematic(mineSchematic, new IntVector3D(0, 0, 0), true);
            ms = System.currentTimeMillis() - ms;
            System.out.println("Generated world in " + ms + "ms");
        });
        customWorld.getBukkitWorld().setSpawnFlags(false, false);
        customWorld.getBukkitWorld().setGameRuleValue("doWeatherCycle", "false");
        customWorld.getBukkitWorld().setPVP(false);
        this.setRegion(new CuboidRegion(customWorld.getBukkitWorld(), mineRegion.getMinimumPoint(), mineRegion.getMaximumPoint()));
        this.reset();
    }

    public void unloadWorld() {
        this.customWorld.getBukkitWorld().getPlayers().forEach(p -> p.teleport(SpigotPrisonCore.instance.getSpawn()));
        this.customWorld.unload();
    }
}
