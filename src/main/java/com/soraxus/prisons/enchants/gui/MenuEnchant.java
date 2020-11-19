package com.soraxus.prisons.enchants.gui;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.api.enchant.EnchantInfo;
import com.soraxus.prisons.pickaxe.crystals.gui.MenuCrystals;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class MenuEnchant extends Menu {
    private Player player;
    private ItemStack toEnchant;
    private List<AbstractCE> enchantments;
    private int buyAmount = 1;

    public MenuEnchant(Player player, ItemStack toEnchant, List<AbstractCE> enchantments) {
        super("Enchant", 5);
        this.player = player;
        this.toEnchant = toEnchant;
        this.enchantments = enchantments;
        this.setup(new AtomicInteger(0));
    }

    public void setup(AtomicInteger page) {
        this.setAll(null);

        this.setElement(0, new MenuElement(new ItemBuilder(Material.DOUBLE_PLANT, 1)
                .setName("§6Tokens: " + NumberUtils.formatFull(Economy.tokens.getBalance(player.getUniqueId())))
                .build()
        ));

        this.setElement(4, new MenuElement(toEnchant));

        this.setElement(2, new MenuElement(new ItemBuilder(Material.ENCHANTED_BOOK, buyAmount)
                .setName("&e&lBuy Amount")
                .addLore("&fCurrently: &a" + buyAmount)
                .addLore("&7The amount you will buy with one click")
                .addLore("&eLeft Click - Increase")
                .addLore("&eRight Click - Decrease").build())
                .setClickHandler((e, i) -> {
                    if (e.getClick().isRightClick() && buyAmount > 1) {
                        buyAmount--;
                    } else if (e.getClick().isLeftClick() && buyAmount < 50) {
                        buyAmount++;
                    } else {
                        return;
                    }
                    this.setup(page);
                    ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_HAT, 0.5f, 0.6f);
                }));

        this.setElement(6, new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&e&lRepair")
                .addLore("&7Click to Repair").build()).setClickHandler((e, i) -> {
            toEnchant.setDurability((short) 0);
            this.setup(page);
            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_HAT, 0.5f, 0.6f);
        }));

        this.setElement(8, new MenuElement(new ItemBuilder(Material.NETHER_STAR)
                .setName("&b&lCrystals")
                .addLore("&7Use these crystals to add additional boosters to your pickaxe")
                .build()
        ).setClickHandler((e, i) -> {
            MenuCrystals menuCrystals = new MenuCrystals(this);
            menuCrystals.setup();
            menuCrystals.open(player);
            this.setup(page);
        }));

        EnchantInfo info = AbstractCE.getInfo(toEnchant);
        this.setupActionableList(19, 9 * 4 - 2, 9 * 4, 9 * 5 - 1, (index -> {
            if (index >= enchantments.size())
                return null;
            AbstractCE ce = enchantments.get(index);

            ItemBuilder builder = new ItemBuilder(Material.BOOK, 1).setName(ce.getDisplayName());

            int level = info.getEnchants().getOrDefault(ce, 0);

            builder.addLore("&fCurrent Level: &a" + level);
            builder.addLore("&fMax Level: &c" + ce.getMaxLevel());
            builder.addLore("");

            builder.addLore("&fDescription:");
            for (String descLine : ce.getDescription())
                builder.addLore("&7" + descLine);
            builder.addLore("");


            if (level != ce.getMaxLevel()) {
                long cost = ce.getCost(level + 1);
                builder.addLore("&fCost: &a" + NumberUtils.toReadableNumber(BigInteger.valueOf(cost)));
                builder.addLore("");
                builder.addLore("&8Click to Buy");
            } else {
                builder.addLore("&cMax Level Reached");
            }

            if (!ce.isEnabled()) {
                builder.addLore("",
                        "&c&lWARNING: &7This enchant is currently",
                        "&7disabled, you can still purchase it, however",
                        "&7it may not work for a while");
            }

            return new MenuElement(builder.build()).setClickHandler((e, i) -> {

                int enchLevel = info.getEnchants().getOrDefault(ce, 0);

                boolean buyable = enchLevel < ce.getMaxLevel();
                if (!buyable)
                    return;

                for (int in = 0; in < buyAmount; in++) {

                    buyable = enchLevel < ce.getMaxLevel();

                    if (buyable) {
                        Player player = (Player) e.getWhoClicked();
                        if (!Economy.tokens.hasBalance(player.getUniqueId(), ce.getCost(enchLevel))) {
                            player.sendMessage("§cInsufficient tokens.");
                            return;
                        }
                        Economy.tokens.removeBalance(player.getUniqueId(), ce.getCost(enchLevel));

                        toEnchant.setItemMeta(ce.enchant(toEnchant, enchLevel + 1).getItemMeta());
                        enchLevel++;
                    }
                    if (in == buyAmount - 1) {
                        this.setup(page);
                        Player p = (Player) e.getWhoClicked();
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
                    }
                }
            });

        }), page);

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build()));

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
