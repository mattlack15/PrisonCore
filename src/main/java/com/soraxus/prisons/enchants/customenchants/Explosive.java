package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.ModuleBreak;
import com.soraxus.prisons.enchants.ModuleEnchants;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.api.enchant.EnchantInfo;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.pickaxe.levels.PickaxeLevelManager;
import com.soraxus.prisons.util.EventSubscription;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Explosive extends AbstractCE {
    private double percentIncrease;

    @Override
    protected void onEnable() {
        percentIncrease = getConfig().getDouble("percent-increase-per-level") / 100d;
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public long getCost(int level) {
        return 0;
    }

    @Override
    public String getName() {
        return "Explosive";
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
        return true;
    }

    private Random rand = new Random(System.currentTimeMillis());

    private List<UUID> fb = new ArrayList<>();

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
        if (rand.nextDouble() > info.getEnchants().get(this) * percentIncrease)
            return;

        Mine mine = MineManager.instance.getMineOf(event.getBlock().getLocation());
        if (mine != null) {
            int radius = 4;
            Vector3D starting = Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector());
            CuboidRegion region = new CuboidRegion(event.getPlayer().getWorld(), starting.add(radius, radius, radius), starting.add(-radius, -radius, -radius));
            AsyncWorld world = new SpigotAsyncWorld(event.getPlayer().getWorld());
            Map<IntVector3D, Integer> blocks = new ConcurrentHashMap<>();
            AtomicInteger total = new AtomicInteger();
            final double radSqrd = radius * radius;

            Map<Integer, AtomicInteger> broken = new ConcurrentHashMap<>();

            world.syncForAllInRegion(region, (v, b) -> {
                if (!(v.distanceSq(starting.asIntVector()) > radSqrd) && mine.getRegion().contains(new Vector3D(v))) {
                    if (b == 0)
                        return;

                    broken.putIfAbsent(b, new AtomicInteger());
                    broken.get(b).incrementAndGet();

                    world.setBlock(v.getX(), v.getY(), v.getZ(), 0, (byte) 0);
                    total.getAndIncrement();
                    if (rand.nextInt(10) < 3)
                        blocks.put(v, b);
                }
            }, true);
            world.flush().thenAccept((n) -> new BukkitRunnable() {
                @Override
                public void run() {
                    blocks.forEach((v, b) -> {
                        Vector3D vel = new Vector3D(v).subtract(starting.subtract(0, 1.2, 0)).normalize().multiply(0.9);
                        Location location = new Location(event.getBlock().getWorld(), v.getX(), v.getY(), v.getZ());
                        if (vel.lengthSq() == 0)
                            return;
                        FallingBlock block = event.getPlayer().getWorld().spawnFallingBlock(location, b & 4095, (byte) (b >> 12));
                        block.setDropItem(false);
                        block.setInvulnerable(true);
                        block.setHurtEntities(false);
                        fb.add(block.getUniqueId());
                        block.setVelocity(vel.toBukkitVector());
                    });
                }
            }.runTask(ModuleEnchants.instance.getParent()));

            event.getBlock().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, event.getBlock().getLocation(), 1);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1f);
            mine.incrementBlocksMined(total.get());
            PickaxeLevelManager.addXp(event.getPlayer(), total.get());

            //Call events
            broken.forEach((k, v) -> ModuleBreak.instance.onBreak(event.getPlayer(), Vector3D.fromBukkitVector(event.getBlock().getLocation().toVector()), k, v.get()));
        }
    }

    @Override
    public void onUnenchant(ItemStack stack) {
    }

    @Override
    public void onEnchant(ItemStack stack, int level) {
    }
}
