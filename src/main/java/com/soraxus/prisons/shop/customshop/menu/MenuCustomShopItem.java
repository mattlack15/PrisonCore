package com.soraxus.prisons.shop.customshop.menu;

import com.soraxus.prisons.shop.customshop.CustomShopSection;
import com.soraxus.prisons.shop.customshop.ShopItem;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuCustomShopItem extends Menu {

    private static final List<Long> updateAmounts = new ArrayList<>();
    private static final long MAX_PRICE = 1000000000000000000L;

    static {
        updateAmounts.add(1L);
        updateAmounts.add(10L);
        updateAmounts.add(100L);
        updateAmounts.add(1000L);
        updateAmounts.add(10000L);
        updateAmounts.add(100000L);
        updateAmounts.add(1000000L);
        updateAmounts.add(10000000L);
        updateAmounts.add(100000000L);
        updateAmounts.add(1000000000L);
        updateAmounts.add(10000000000L);
        updateAmounts.add(100000000000L);
        updateAmounts.add(1000000000000L);
        updateAmounts.add(10000000000000L);
        updateAmounts.add(100000000000000L);
        updateAmounts.add(1000000000000000L);
        updateAmounts.add(10000000000000000L);

        updateAmounts.sort(Comparator.comparingLong((l) -> l));
    }

    private final ShopItem item;
    private final CustomShopSection parentShop;
    private final MenuElement backElement;
    private final AtomicInteger editIndex = new AtomicInteger();


    public MenuCustomShopItem(CustomShopSection parentShop, ShopItem item, MenuElement backElement) {
        this.item = item;
        this.parentShop = parentShop;
        this.backElement = backElement;
        this.setTitle("");
        this.setSize(3);
    }

    @Override
    public void build(UUID player) {
        MenuElement remove = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&cRemove").addLore("&7Click to remove this shop item").build())
                .setClickHandler((e, i) -> {
                    parentShop.getItems().remove(item);
                    if(backElement == null) {
                        e.getWhoClicked().closeInventory(); //Close inventory if there is no back element
                    } else {
                        backElement.getClickHandler().handleClick(e, i); //Get the back element to handle the click
                    }
                });

        MenuElement price = new MenuElement(new ItemBuilder(Material.EMERALD).setName("&a$" + NumberUtils.toReadableNumber(item.getCost())).addLore("&8Editing by &f" +
                NumberUtils.toReadableNumber(updateAmounts.get(editIndex.get())) + " &7(Shift R/L Click)").addLore("", "&fLeft Click to &aincrease", "&fRight Click to&c decrease").build())
                .setClickHandler((e, i) -> {
                    if(e.getClick().isShiftClick()) {
                        //Editing edit amount
                        if(e.getClick().isLeftClick()) {
                            //Increase
                            int max = updateAmounts.size() - 1;
                            if(editIndex.get() >= max) {
                                return;
                            }
                            editIndex.incrementAndGet();
                        } else {
                            //Decrease
                            int min = 0;
                            if(editIndex.get() <= min) {
                                return;
                            }
                            editIndex.decrementAndGet();
                        }
                    } else {
                        //Editing price
                        if(e.getClick().isLeftClick()) {
                            //Increase
                            item.setCost(Math.min(MAX_PRICE, item.getCost() + updateAmounts.get(editIndex.get()))); //Min it with max price
                        } else {
                            //Decrease
                            item.setCost(Math.max(0, item.getCost() - updateAmounts.get(editIndex.get()))); //Max it with 0 to prevent negative prices
                        }
                    }
                    build(player);
                    MenuManager.instance.invalidateInvsForMenu(this);
                });

        ItemBuilder storageItem = new ItemBuilder(Material.CHEST).setName("&6Storage")
                .addLore(item.getStock() == -1 ? "&cInfinite" : "&f" + NumberUtils.toReadableNumber(item.getStock()), "", "&fLeft Click &7to open storage");
        MenuElement storage;
        if(Bukkit.getPlayer(player) != null && Bukkit.getPlayer(player).hasPermission("globalshop.infstock")) {
            storageItem.addLore("&fRight Click &7to toggle&c infinite stock");
            storage = new MenuElement(storageItem.build()).setClickHandler((e, i) -> {
                if(e.getClick().isLeftClick()) {
                    new MenuShopItemInventory(item).open(e.getWhoClicked());
                } else if(e.getClick().isRightClick()) {
                    if(item.getStock() == -1) {
                        item.setStock(0);
                    } else {
                        item.setStock(-1);
                    }
                }
                build(player);
                MenuManager.instance.invalidateInvsForMenu(this);
            });
        } else {
            storage = new MenuElement(storageItem.build())
                .clickBuilder().openMenu(new MenuShopItemInventory(item)).build();
        }


        this.setElement(11, storage)
                .setElement(13, price)
                .setElement(15, remove)
                .setElement(4, backElement);

    }
}
