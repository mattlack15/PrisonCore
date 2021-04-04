package com.soraxus.prisons.shop.customshop.menu;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.shop.customshop.CustomShopSection;
import com.soraxus.prisons.shop.customshop.ShopItem;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuCustomShopSection extends Menu {

    private final CustomShopSection shop;
    private final boolean editing;
    private final MenuElement backButton;

    public MenuCustomShopSection(CustomShopSection shop, MenuElement backButton, boolean editing) {
        super(shop.getName(), 3);
        this.shop = shop;
        this.editing = editing;
        this.backButton = backButton;
        this.setup();
    }

    public MenuCustomShopSection setup() {
        this.setSize(6);
        this.setAll(null);
        this.setElement(4, backButton);

        List<ShopItem> items = shop.getItems().copy();

        this.setupActionableList(9, 44, 45, 45 + 8, (index) -> {
            if (index > items.size()) {
                return null;
            } else if (index == items.size()) {
                if(!editing)
                    return null;
                //Placeholder
                ItemBuilder builder = new ItemBuilder(Material.STAINED_GLASS_PANE).setName("&a&lNew")
                        .addLore("&7Put something here to add");

                return new MenuElement(builder.build()).clickBuilder().onLeftClick((e) -> {
                    if(e.getCursor() == null || e.getCursor().getType() == Material.AIR)
                        return;
                    ItemStack i = e.getCursor().clone();
                    i.setAmount(1);
                    ShopItem item = new ShopItem(i, ShopItem.ShopItemType.BUY);
                    shop.getItems().add(item);
                    setup();
                    e.getWhoClicked().setItemOnCursor(e.getCursor());
                }).build();
            } else {
                //Item
                ShopItem item = items.get(index);
                ItemBuilder builder = new ItemBuilder(item.getItem());

                if (item.getStock() != 0) {
                    if (!editing) {
                        builder.addLore("", "&aClick to buy for &a$" + NumberUtils.toReadableNumber(item.getCost()));
                    } else {
                        builder.addLore("", "&a$" + NumberUtils.toReadableNumber(item.getCost()),
                                "&8Click to edit");
                    }
                } else {
                    builder.addLore("", "&cOut of Stock!");
                }
                if (editing) {
                    return new MenuElement(builder.build()).clickBuilder().openMenu(new MenuCustomShopItem(shop, item, getBackButton(this)
                    .setClickHandler((e, i) -> {
                        setup();
                        open(e.getWhoClicked());
                    }))).build();
                } else {
                    return new MenuElement(builder.build()).clickBuilder().onLeftClick((e) -> {
                        //Buy 1
                        if (item.getStock() == -1 || item.getStock() >= 1) {
                            long cost = item.getCost();
                            Player p = (Player) e.getWhoClicked();

                            //Check for inventory space
                            if(p.getInventory().firstEmpty() == -1) {
                                new ChatBuilder("&cYou don't have enough inventory space!").send(p);
                                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 0.9f);
                                return;
                            }

                            //Try to remove balance
                            if (Economy.money.tryRemoveBalance(p.getUniqueId(), cost)) {
                                //Transaction completed
                                if (item.getStock() != -1) { //Finite stock
                                    item.setStock(item.getStock() - 1);
                                }

                                //Add item
                                ItemStack i = item.getItem().clone();
                                i.setAmount(1);
                                p.getInventory().addItem(i);
                                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.6f, 1.65f);
                            } else {
                                //Insufficient balance
                                new ChatBuilder("&cInsufficient money!").send(p);
                                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 0.9f);
                            }
                        }
                        setup();
                    }).onShiftLeftClick((e) -> {
                        //Buy a stack
                        int stackSize = item.getItem().getMaxStackSize();
                        if (item.getStock() == -1 || item.getStock() >= stackSize) {
                            long cost = item.getCost() * stackSize;
                            Player p = (Player) e.getWhoClicked();
                            if(p.getInventory().firstEmpty() == -1) {
                                new ChatBuilder("&cYou don't have enough inventory space!").send(p);
                                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 0.9f);
                                return;
                            }
                            if (Economy.money.tryRemoveBalance(p.getUniqueId(), cost)) {
                                if (item.getStock() != -1) { //Finite stock
                                    item.setStock(item.getStock() - stackSize);
                                }
                                ItemStack i = item.getItem().clone();
                                i.setAmount(stackSize);
                                p.getInventory().addItem(i);
                                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.6f, 1.65f);
                            } else {
                                new ChatBuilder("&cInsufficient money!").send(p);
                                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 0.9f);
                            }
                        }
                        setup();
                    }).build();
                }
            }
        }, 0);

        MenuManager.instance.invalidateInvsForMenu(this);
        return this;
    }
}
