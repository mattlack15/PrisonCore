package com.soraxus.prisons.bunkers.shop;

import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class BunkerShopMenu extends Menu {
    private final BunkerShop shop;
    private MenuElement backButton;

    public BunkerShopMenu(BunkerShop shop, MenuElement backButton) {
        super(shop.getName(), 6);
        this.shop = shop;
        this.backButton = backButton;
        this.setup(0);
    }

    public void setup(int sectionIndex) {
        this.setAll(null);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        BunkerShopSection currentSection = shop.getSections().get(sectionIndex);

        int i = 10;
        for (int l = 0; l < shop.getSections().size(); l++) {
            BunkerShopSection section = shop.getSections().get(l);
            if (i % 9 == 8) {
                i = (i - (i % 9)) + 9 + 1; //Make i = to next line but at the same col as starting position
            }
            ItemStack stack = new ItemBuilder(section.getDisplayItem()).addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build();
            if (sectionIndex == l) {
                stack = new ItemBuilder(stack)
                        .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        .build();
            }
            final int finalL = l;
            this.setElement(i, new MenuElement(stack)
                    .setClickHandler((event, invInfo) -> {
                        setup(finalL);
                        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 0.75f, 1.05f);
                    }));
            i++;
        }

        int minMargin = 2;
        int margin = minMargin;

        //Get an appropriate looking margin size
        if (currentSection.getItems().size() < 9 - minMargin * 2) {
            margin = (int) Math.ceil((9 - currentSection.getItems().size()) / 2d);
        }

        //
        int pos = 27 + margin;
        int elementNum = 0;

        for (BunkerShopItem items : currentSection.getItems()) {
            if (pos % 9 == 9 - margin) { //Increment line

                //Get an appropriate looking margin size
                if (currentSection.getItems().size() - elementNum < 9 - minMargin * 2) {
                    margin = (int) Math.ceil((9 - currentSection.getItems().size() + elementNum) / 2d);
                }

                pos = (pos - (pos % 9)) + 9 + margin; //Make pos = to next line but at the new (if modified) margin
            }

            this.setElement(pos, new MenuElement(new ItemBuilder(items.getItemStack()).addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build()).setClickHandler(((event, info) -> {
                Player p = ((Player) event.getWhoClicked());

                if (event.getClick().equals(ClickType.DOUBLE_CLICK)) {
                    return;
                }

                //Check and remove needed
                if (!items.getRequirement().get()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 2f, 1f);
                    this.getElement(event.getSlot()).addTempLore(this, "&cYou don't have enough materials to buy this!", 60);
                    return;
                }

                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                items.getGiver().accept(p);
                setup(sectionIndex);
            })));

            pos++;
            elementNum++;
        }

        this.setElement(4, backButton);

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
