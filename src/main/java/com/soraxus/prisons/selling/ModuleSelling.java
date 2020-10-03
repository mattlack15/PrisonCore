package com.soraxus.prisons.selling;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.event.PrisonPreSellEvent;
import com.soraxus.prisons.selling.autosell.AutoSellInfo;
import com.soraxus.prisons.selling.autosell.AutoSellManager;
import com.soraxus.prisons.selling.autosell.command.AutoSellCmd;
import com.soraxus.prisons.selling.mutlipliers.Multiplier;
import com.soraxus.prisons.selling.mutlipliers.MultiplierInfo;
import com.soraxus.prisons.selling.mutlipliers.MultiplierManager;
import com.soraxus.prisons.selling.mutlipliers.command.CommandMultiplier;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ModuleSelling extends CoreModule {
    public static ModuleSelling instance;

    @Getter
    private AutoSellManager autoSellManager = new AutoSellManager(this);
    @Getter
    private MultiplierManager multiplierManager = new MultiplierManager();

    private Map<UUID, Long> cooldowns = new HashMap<>();

    @Getter
    private List<SellItem> prices = new ArrayList<>();

    @Override
    public String getName() {
        return "Selling";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        instance = this;
        multiplierManager.startLoop();
        reloadPrices();
        new AutoSellCmd().register();
        new CommandMultiplier();
    }

    public void reloadPrices() {
        prices.clear();

        FileConfiguration config = getConfig();
        ConfigurationSection sellprices;
        if (config.contains("sellprices")) {
            sellprices = getConfig().getConfigurationSection("sellprices");
        } else {
            sellprices = getConfig().createSection("sellprices");
            saveConfig();
        }
        for (String key : sellprices.getKeys(false)) {
            prices.add(SellItem.fromSection(sellprices.getConfigurationSection(key)));
        }
    }

    public void savePrices() {
        FileConfiguration config = getConfig();
        config.set("sellprices", null);
        ConfigurationSection sellprices = config.createSection("sellprices");
        int i = 0;
        for (SellItem item : prices) {
            item.saveTo(sellprices.createSection((i++) + ""));
        }
    }

    public long getPrice(ItemStack item) {
        if (item == null) {
            return -1;
        }
        for (SellItem price : prices) {
            if (!item.getType().equals(price.getItemMaterial())) {
                continue;
            }
            if (item.getData() == null) {
                continue;
            }
            if (item.getData().getData() != price.getItemData()) {
                continue;
            }
            return price.getPrice() * item.getAmount();
        }
        return -1;
    }

    public void sellall(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        long tot = 0;
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            long price = getPrice(contents[i]);
            if (price != -1) {
                contents[i] = null;
                tot += price;
                items.add(contents[i]);
            }
        }
        final double multi = new MultiplierInfo(player).getTotal();
        PrisonPreSellEvent event = new PrisonPreSellEvent(player, items, multi);
        Bukkit.getPluginManager().callEvent(event);
        player.getInventory().setContents(contents);
        player.sendMessage("§5§l            SELL SUMMARY");
        player.sendMessage("§8§m------------------------");
        player.sendMessage("§dOriginal: §f" + NumberUtils.formatFull(tot));
        player.sendMessage("§dMultiplier: §f" + event.getMultiplier());
        tot *= (long) event.getMultiplier();
        player.sendMessage("§dTotal: §f" + NumberUtils.formatFull(tot));
        player.sendMessage("§8§m------------------------");
        Economy.money.addBalance(player.getUniqueId(), tot);
    }

    @EventSubscription(priority = EventPriority.HIGH)
    private void onBreak(PrisonBlockBreakEvent event) {
        AutoSellInfo info = this.getAutoSellManager().getInfo(event.getPlayer());
        if (info.isEnabled()) {
            info.getLock().lock();
            getAutoSellManager().autosellNow(event.getPlayer(), ItemUtils.fromBlock(event.getBlock(), event.getAmount()));
            info.getLock().unlock();
            event.setAmount(0);
        }
    }

    @EventSubscription(priority = EventPriority.HIGH)
    private void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getHand().equals(EquipmentSlot.HAND)) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (Multiplier.isMultiplier(item)) {
                    e.setCancelled(true);
                    Multiplier multi = Multiplier.parseFromItem(item);
                    if (MultiplierManager.instance.tryActivate(player, multi)) {
                        ItemUtils.decrementHand(player);
                    }
                }
            }
            return;
        }
        if (player.isSneaking()) {
            if (!cooldowns.containsKey(player.getUniqueId()))
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() - 2000);
            if (System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < 2000) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Sell > " + ChatColor.RED + "You must wait " + Math.round((2000 - (System.currentTimeMillis() - cooldowns.get(player
                        .getUniqueId()))) / 1000d) + " seconds before using this again!");
                return;
            }
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            sellall(player);
        }
    }

    public ConfigurationSection getMultiplierData() {
        return getConfig().getConfigurationSection("multiplier");
    }
}
