package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.ModuleBreak;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.api.enchant.EnchantInfo;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.pickaxe.levels.PickaxeLevelManager;
import com.soraxus.prisons.util.EventSubscription;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Laser extends AbstractCE {
    private double percentIncrease;

    @Override
    protected void onEnable() {
        this.percentIncrease = getConfig().getDouble("percent-increase-per-level") / 100d;
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
        return "Laser";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    private Random rand = new Random(System.currentTimeMillis());

    List<Vector3D> rayTrace(Vector3D start, Vector3D direction, CuboidRegion bounds) {
        List<Vector3D> vectors = new ArrayList<>();
        direction = direction.normalize();
        for(double i = 0; i < 100; i += 0.2) {
            Vector3D pos = start.add(direction.multiply(i));
            if(!bounds.contains(pos))
                break;
            vectors.add(pos);
        }
        return vectors;
    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand() == null || !hasEnchant(event.getPlayer().getItemInHand()))
            return;

        EnchantInfo info = getInfo(event.getPlayer().getInventory().getItemInMainHand());
        if(rand.nextDouble() > info.getEnchants().get(this) * percentIncrease)
            return;

        Mine mine = MineManager.instance.getMineOf(event.getBlock().getLocation());
        if (mine != null) {
            AsyncWorld world = new SpigotAsyncWorld(event.getPlayer().getWorld());
            List<Vector3D> vecs = rayTrace(Vector3D.fromBukkitVector(event.getPlayer().getEyeLocation().toVector()), Vector3D.fromBukkitVector(event.getPlayer().getLocation().getDirection()), mine.getRegion());
            AtomicInteger broken = new AtomicInteger();
            vecs.forEach(b -> {
                int block = world.syncGetBlock(b.getBlockX(), b.getBlockY (), b.getBlockZ());
                ModuleBreak.instance.onBreak(event.getPlayer(), b, block);
                world.setBlock(b.getBlockX(), b.getBlockY(), b.getBlockZ(), 0, (byte) 0);
                broken.getAndIncrement();
            });
            world.flush();
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.6f, 1f);
            mine.incrementBlocksMined(broken.get() - 1);
            PickaxeLevelManager.addXp(event.getPlayer(), broken.get() - 1);
        }
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }

    @Override
    public void onUnenchant(ItemStack stack) {

    }

    @Override
    public void onEnchant(ItemStack stack, int level) {

    }
}
