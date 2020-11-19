package com.soraxus.prisons.enchants.customenchants;

//public class Endowment extends AbstractCE {
//    private double percentIncrease;
//
//    @Override
//    protected void onEnable() {
//        percentIncrease = getConfig().getDouble("percent-increase-per-level") / 100d;
//    }
//
//    @Override
//    protected void onDisable() {
//
//    }
//
//    @Override
//    public long getCost(int level) {
//        return (long) (100 * Math.pow(level, 2));
//    }
//
//    @Override
//    public String getName() {
//        return "Endowment";
//    }
//
//    @Override
//    public int getStartLevel() {
//        return 1;
//    }
//
//    @Override
//    public EnchantmentTarget getItemTarget() {
//        return EnchantmentTarget.TOOL;
//    }
//
//    @Override
//    public boolean canEnchantItem(ItemStack itemStack) {
//        return true;
//    }
//
//    @EventSubscription
//    private void onBreak(PrisonBlockBreakEvent event) {
//        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
//        if (hasEnchant(item)) {
//            int level = getLevel(item);
//            double chance = 0.01 + percentIncrease * level;
//            if (MathUtils.isRandom(chance, 100.0)) {
//                long amount = MathUtils.random(level * 5, level * 10);
//                String amFormat = NumberUtils.formatFull(amount);
//                for (Player player : Bukkit.getOnlinePlayers()) {
//                    if (!player.getUniqueId().equals(event.getPlayer().getUniqueId())) { //You don't get money from your own charity
//                        player.sendMessage("You received " + amFormat + "$ from " + event.getPlayer().getName() + "'s Endowment Enchantment");
//                        Economy.money.addBalance(player.getUniqueId(), amount);
//                    }
//                }
//                event.getPlayer().sendMessage("Your endowment gave everyone " + amFormat + "$");
//            }
//        }
//    }
//
//    @Override
//    public void onUnenchant(ItemStack stack) {
//
//    }
//
//    @Override
//    public void onEnchant(ItemStack stack, int level) {
//
//    }
//}
