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
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class JackHammer extends AbstractCE {
    private double percentIncrease = 0.0001;
    private long cost;

    @Override
    protected void onEnable() {
        percentIncrease = getConfig().getDouble("percent-increase-per-level") / 100d;
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public long getCost(int level) {
        return (long) (1000 * Math.pow(level, 2));
    }

    @Override
    public String getName() {
        return "Jack Hammer";
    }

    private ExecutorService service = Executors.newSingleThreadExecutor();

    private Random rand = new Random(System.currentTimeMillis());

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand() == null || !hasEnchant(event.getPlayer().getItemInHand()))
            return;

        EnchantInfo info = getInfo(event.getPlayer().getInventory().getItemInMainHand());
        if(rand.nextDouble() > info.getEnchants().get(this) * percentIncrease)
            return;

        Mine mine = MineManager.instance.getMineOf(event.getBlock().getLocation());
        if (mine != null) {
            CuboidRegion region = mine.getRegion();

            CuboidRegion hammerRegion = region.clone();
            hammerRegion.clampY(event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockY());

            AsyncWorld session = new SpigotAsyncWorld(event.getBlock().getWorld());

            AtomicInteger broken = new AtomicInteger();
            session.syncForAllInRegion(hammerRegion, (v, block) -> {
                        if (block == 0)
                            return;
                        broken.getAndIncrement();
                        ModuleBreak.instance.onBreak(event.getPlayer(), v, block);
                    }, true);

            service.execute(() -> {
                session.setBlocks(hammerRegion, () -> (short) 0);
                session.flush();
            });
            mine.incrementBlocksMined(broken.get() - 1);
            PickaxeLevelManager.addXp(event.getPlayer(), broken.get() - 1);

        }
    }

    private static class UCounter {
        public int counter = 0;
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

    @Override
    public void onUnenchant(ItemStack stack) {
    }

    @Override
    public void onEnchant(ItemStack stack, int level) {
    }
}
