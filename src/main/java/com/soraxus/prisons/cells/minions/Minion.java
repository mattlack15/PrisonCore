package com.soraxus.prisons.cells.minions;

import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Minion implements GravSerializable {
    @Getter
    private final MinionManager parent;
    @Getter
    private final AtomicInteger stored = new AtomicInteger();
    @Getter
    private final MinionArmorStand standController;
    @Getter
    private IntVector3D location;
    @Getter
    private int direction = 0; // 0: X+, Clockwise
    @Setter
    @Getter
    private String name = "Minion";
    @Getter
    @Setter
    private MinionSettings settings;
    @Getter
    @Setter
    @Nullable
    private UUID creator;
    @Getter
    @Setter
    private volatile double speed = 1.0;
    @Getter
    @Setter
    private volatile ItemType miningBlock = new ItemType(Material.STONE);
    @Getter
    private volatile boolean built;
    private int timer = 0;

    Minion(MinionManager parent, IntVector3D location) {
        this.parent = parent;
        this.location = location;
        this.settings = new MinionSettings();
        this.standController = new MinionArmorStand(this);
        this.standController.spawn();
    }

    Minion(GravSerializer serializer, MinionManager parent) {
        this.parent = parent;
        this.location = serializer.readObject();
        this.name = serializer.readString();

        try {
            serializer.mark();
            this.miningBlock = serializer.readObject();
        } catch (Exception e) {
            serializer.reset();
            e.printStackTrace();
        }

        this.settings = serializer.readObject();
        this.speed = serializer.readDouble();
        this.stored.set(serializer.readInt());
        this.creator = serializer.readObject();
        this.direction = serializer.readInt();
        this.standController = new MinionArmorStand(this);
    }

    public IntVector3D getMiningBlockLocation() {
        int x = 0;
        int z = 0;
        switch (direction) {
            case 0:
                x = 1;
                break;
            case 1:
                z = 1;
                break;
            case 2:
                x = -1;
                break;
            case 3:
                z = -1;
                break;
        }
        return location.add(x, 0, z);
    }

    public void spawn() {
        if (!Bukkit.isPrimaryThread())
            throw new RuntimeException("This method must be called in the bukkit main thread");
        if (built)
            destroy();

        this.standController.spawn();
        Block block = this.getMiningBlockLocation().toBukkitVector().toLocation(this.getParent().getParent().getWorld().getBukkitWorld())
                .getBlock();

        BlockState state = block.getState();
        state.setType(this.miningBlock.getMat());
        state.setData(this.miningBlock.getMat().getNewData(this.miningBlock.getData()));
        state.update(true);

        this.built = true;
        EventSubscriptions.instance.subscribe(this);
    }

    public void destroy() {
        if (!Bukkit.isPrimaryThread())
            throw new RuntimeException("This method must be called in the bukkit main thread");
        if (!built)
            spawn();

        this.standController.destroy();

        Block block = this.getMiningBlockLocation().toBukkitVector().toLocation(this.getParent().getParent().getWorld().getBukkitWorld())
                .getBlock();
        BlockState state = block.getState();
        state.setType(Material.AIR);
        state.update(true);

        this.built = false;
        EventSubscriptions.instance.unSubscribeAll(this);
    }

    public void remove() {
        if (!Bukkit.isPrimaryThread())
            throw new RuntimeException("This method must be called in the bukkit main thread");
        if (this.isBuilt())
            this.destroy();
        parent.remove0(this);
    }

    public void messagePlayer(Player player, String message) {
        player.sendMessage("§cMinion > " + ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(location);
        serializer.writeString(name);
        serializer.writeObject(getMiningBlock());
        serializer.writeObject(settings);
        serializer.writeDouble(speed);
        serializer.writeInt(stored.get());
        serializer.writeObject(creator);
        serializer.writeInt(direction);
    }

    public void update() {
        int period = (int) Math.max(1, 40 / speed);
        if (timer++ >= period) {
            timer = 0;
            int amount = 1;
            if (period == 1) {
                amount = (int) (speed / 40D);
            }
            amount *= ModuleSelling.instance.getPrice(new ItemStack(getMiningBlock().getMat(), 1, getMiningBlock().getData()));
            amount = Math.max(amount, 0);
            int finalAmount = amount;
            this.getStored().getAndUpdate((c) -> {
                if (Integer.MAX_VALUE - c >= finalAmount) {
                    return c + finalAmount;
                }
                return c;
            });
        }

        double progress = timer / (double) period;
        if (progress == 1) {
            World world = parent.getParent().getWorld().getBukkitWorld();
            world.spawnParticle(
                    Particle.BLOCK_CRACK,
                    getMiningBlockLocation().toBukkitVector()
                            .add(new Vector(0.5, 0.5, 0.5))
                            .toLocation(world),
                    24,
                    0.2, 0.2, 0.2,
                    getMiningBlock().asMaterialData()
            );
        } else {
            sendBlockBreakUpdate(progress);
        }
        if (period < 5) {
            progress = this.standController.getPosition() + 0.15D;
            if (progress > 1) {
                progress = 0;
            }
        }

        this.standController.setPosition(progress);
    }

    public ItemStack asItemStack() {
        return MinionItems.getMinionItem(this.name, this.miningBlock, this.speed, this.settings);
    }

    private void sendBlockBreakUpdate(double progress) {
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(
                location.hashCode(),
                new BlockPosition(
                        getMiningBlockLocation().getX(),
                        getMiningBlockLocation().getY(),
                        getMiningBlockLocation().getZ()
                ),
                (int) (10 * progress)
        );

        ArmorStand stand = getStandController().getStand();
        if (stand == null) {
            return;
        }

        for (Entity ent : stand.getNearbyEntities(20, 20, 20)) {
            if (ent == null) {
                continue;
            }
            if (ent instanceof Player) {
                Player player = (Player) ent;
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    @EventSubscription
    private void onClick(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().getUniqueId().equals(this.standController.getArmorStand())) {
            if (this.creator == null || this.creator.equals(event.getPlayer().getUniqueId())) {
                new MenuMinion(this).open(event.getPlayer());
            }
        }
    }
}
