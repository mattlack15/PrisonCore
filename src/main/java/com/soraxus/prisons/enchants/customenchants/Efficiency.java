package com.soraxus.prisons.enchants.customenchants;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.util.EventSubscription;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Efficiency extends AbstractCE {

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public long getCost(int level) {
        return (long) (100 * Math.pow(level, 2));
    }

    @Override
    public void onEnchant(ItemStack stack, int level) {
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        stack.addUnsafeEnchantment(Enchantment.DIG_SPEED, level);
    }

    @Override
    public void onUnenchant(ItemStack stack) {
        stack.removeEnchantment(Enchantment.DIG_SPEED);
    }

    @Override
    public String getName() {
        return "Efficiency";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
//        int x1 = event.getBlock().getLocation().getBlockX();
//        int y1 = event.getBlock().getLocation().getBlockY();
//        int z1 = event.getBlock().getLocation().getBlockZ();
//        CuboidRegion region = new CuboidRegion(event.getBlock().getWorld(),
//                new Vector(x1 - 50, y1 - 50, z1 - 150), new Vector(x1 + 50, y1 + 6, z1 + 50));
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                long nano = System.nanoTime();
//                AsyncWorld session = new AsyncWorld(region.getWorld());
//                boolean sneaking = event.getPlayer().isSneaking();
//                Random rand = new Random(System.currentTimeMillis());
//                session.syncFastRefreshChunksInRegion(region, -1);
////                session.setBlocks(region, () -> sneaking ? (short) (rand.nextInt(2) == 1 ? 1 << 12 | 1 : 2) : 0);
////                session.flush();
//                long took = System.nanoTime() - nano;
//                Bukkit.broadcastMessage("Took " + took + "ns to complete loading of " + region.getArea() + " blocks in memory");
//                int grass = 0;
//                int other = 0;
//                int non = 0;
//                for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getX(); x++) {
//                    for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getZ(); z++) {
//                        for (int y = region.getMinimumPoint().getBlockY(); y <= region.getMaximumPoint().getY(); y++) {
//                            int block = session.getCachedBlock(x, y, z);
//                            if(block == (1 << 12 | 2) || block == 0) {
//                                grass++;
//                            } else if(block == -1) {
//                                non++;
//                            } else {
//                                other++;
//                            }
//                        }
//                    }
//                }
//                Bukkit.broadcastMessage("There are " + non + " non-loaded blocks");
//                Bukkit.broadcastMessage("There are " + grass + " air blocks");
//                Bukkit.broadcastMessage("There are " + other + " other blocks");
//            }
//        }.runTask(ModuleEnchants.instance);
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }
}
