package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.ModuleBreak;
import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.api.enchant.EnchantInfo;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.Synchronizer;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Nuke extends AbstractCE {
    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public long getCost(int level) {
        return (long) (2500 * Math.pow(level, 2.1D));
    }


    @Override
    public String getName() {
        return "Nuke";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getType().toString().contains("PICKAXE");
    }

    @Override
    public void onUnenchant(ItemStack stack) {
    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }

    private final Random rand = new Random(System.currentTimeMillis());
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final List<UUID> fb = Collections.synchronizedList(new ArrayList<>());

    @EventSubscription
    private void onBlockFall(EntityChangeBlockEvent event) {
        if (fb.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
            fb.remove(event.getEntity().getUniqueId());
        }
    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {

        if(event.isCancelled())
            return;

        if (event.getPlayer().getInventory().getItemInMainHand() == null || !hasEnchant(event.getPlayer().getItemInHand()))
            return;

        EnchantInfo info = getInfo(event.getPlayer().getInventory().getItemInMainHand());
        if (rand.nextDouble() > info.getEnchants().get(this) * 0.0001D)
            return;

        Mine mine = MineManager.instance.getMineOf(event.getBlock().getLocation());
        if (mine != null) {
            CuboidRegion region = mine.getRegion();
            AsyncWorld world = new SpigotAsyncWorld(event.getBlock().getWorld());
            Vector3D brokenLoc = Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector());
            service.submit(() -> {
                try {
                    Map<Integer, AtomicInteger> broken1 = new ConcurrentHashMap<>();
                    world.asyncForAllInRegion(region, (loc, block, tag, brightness) -> {
                        broken1.putIfAbsent(block, new AtomicInteger());
                        broken1.get(block).incrementAndGet();
                    }, true);
                    AtomicInteger level = new AtomicInteger(region.getMaximumY());
                    new BukkitRunnable() {
                        public void run() {
                            int l = level.getAndDecrement();
                            if (l < region.getMinimumY()) {
                                mine.reset();
                                this.cancel();
                                return;
                            }
                            CuboidRegion region1 = new CuboidRegion(region.getWorld(), region.getMinimumPoint(), region.getMaximumPoint());
                            region1.clampY(l, l);
                            Map<IntVector3D, Integer> blocks1 = new ConcurrentHashMap<>();
                            world.asyncForAllInRegion(region1, (loc, block, tag, brightness) -> {
                                if (rand.nextInt(20) == 0 && mine.getRegion().getArea() < 65000) {
                                    blocks1.put(loc, block);
                                }
                            }, true);
                            world.setBlocks(region1, () -> (short) 0);
                            world.flush(false).join();
                            Synchronizer.synchronize(() -> blocks1.forEach((lo, b) -> spawnFallingBlock(world.getBukkitWorld(), lo, b, brokenLoc)));
                        }
                    }.runTaskTimerAsynchronously(SpigotPrisonCore.instance, 0, 1);
                    Synchronizer.synchronize(() -> broken1.forEach((k, v) -> ModuleBreak.instance.onBreak(event.getPlayer(), Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector()), k, v.get())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void spawnFallingBlock(World world, IntVector3D position, int blockId, Vector3D negativeAccentVelocity) {
        if (blockId == 0)
            return;
        Location location = new Location(world, position.getX(), position.getY(), position.getZ());
        FallingBlock block = world.spawnFallingBlock(location, blockId & 4095, (byte) (blockId >> 12));
        block.setDropItem(false);
        block.setInvulnerable(true);
        block.setHurtEntities(false);
        fb.add(block.getUniqueId());
        Vector3D vel = Vector3D.fromBukkitVector(position.toBukkitVector()).subtract(negativeAccentVelocity).normalize().multiply(0.4D).setY(0.1D);
        block.setVelocity(vel.toBukkitVector());
        fb.add(block.getUniqueId());
    }
}
