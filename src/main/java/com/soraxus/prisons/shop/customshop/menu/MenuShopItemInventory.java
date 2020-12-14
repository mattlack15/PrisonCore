package com.soraxus.prisons.shop.customshop.menu;

import com.soraxus.prisons.shop.customshop.ShopItem;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MenuShopItemInventory extends Menu {

    private final ShopItem item;

    //NOTE: Don't have to do this currently, as only using as a global shop

    public MenuShopItemInventory(ShopItem item) {
        this.item = item;
        this.setTitle("Item");
        this.setSize(6);
    }

    @Override
    public void build(UUID player) {
        MenuElement deposit = new MenuElement(new ItemBuilder(Material.EMERALD_BLOCK).setName("&aDeposit")
                .addLore("&fClick &8to deposit 1 item from your inventory", "&fShift-click&8 to deposit 64 items from your inventory")
                .build()).setClickHandler((e, i) -> {

        });

        MenuElement withdraw = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK).setName("&cWithdraw")
                .addLore("&fClick to withdraw 1 item", "&fShift-click&8 to withdraw 64 items")
                .build()).setClickHandler((e, i) -> {
            //Check for full inventory
            if(!e.getWhoClicked().getInventory().contains((ItemStack) null)) {
                e.getWhoClicked().sendMessage(ChatColor.RED + "Your inventory doesn't have a free slot!");
                return;
            }

            int amount = e.isShiftClick() ? 64 : 1;
            amount = Math.max(item.getStock(), amount);

            item.setStock(item.getStock() - amount);

            ItemStack stack = item.getItem().clone();
            stack.setAmount(amount);
            e.getWhoClicked().getInventory().addItem(stack);
        });
    }
}
