package com.soraxus.prisons.shop.customshop.menu;

import com.soraxus.prisons.shop.customshop.CustomShop;
import com.soraxus.prisons.shop.customshop.CustomShopSection;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuCustomShop extends Menu {

    private final CustomShop shop;

    private boolean editing = false;

    private final boolean canEdit;

    public MenuCustomShop(CustomShop shop, boolean canEdit) {
        this.shop = shop;
        this.canEdit = canEdit;

        this.setSize(5);
        this.setTitle(shop.getName());
    }

    @Override
    public void build(UUID player) {
        this.setAll(null);
        int rows = this.getRows();

        if (rows < 2)
            return;

        List<CustomShopSection> sections = shop.getSections().copy();

        int row = 1;
        List<MenuElement> todo = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {

            CustomShopSection s = sections.get(i);

            ItemBuilder builder = new ItemBuilder(s.getDisplayStack()).
                    setName("&e&l" + s.getName())
                    .addLore("&7Click to&e open");
            if(editing) {
                builder.addLore("&fShift-Right Click to&c delete");
            }

            todo.add(new MenuElement(builder.build())
                    .clickBuilder()
                    .openMenu(new MenuCustomShopSection(s, getBackButton(this), editing))
                    .onShiftRightClick((e) -> {
                        if(editing) {
                            shop.getSections().remove(s);
                            build(player);
                            invalidate();
                        } else {
                            new MenuCustomShopSection(s, getBackButton(this), editing).open(e.getWhoClicked());
                        }
                    })
                    .build());

            if (todo.size() == 4) {
                evenlyDistribute(row, todo.toArray(new MenuElement[0]));
                todo.clear();
                row++;
            }
        }

        if(editing) {
            todo.add(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE).setName("&a&lAdd Section").addLore("&7Put something here to add a section").build())
                    .clickBuilder().onLeftClick((e) -> {
                        if (e.getCursor() == null || e.getCursor().getType() == Material.AIR)
                            return;
                        ItemStack i = e.getCursor().clone();
                        i.setAmount(1);
                        CustomShopSection shopSection = new CustomShopSection("Section #" + shop.getSections().size(), i);
                        shop.getSections().add(shopSection);
                        e.setCancelled(true);
                        build(player);
                        invalidate();
                    }).build());
        }

        evenlyDistribute(row, todo.toArray(new MenuElement[0]));
        if(canEdit) {
            MenuElement edit = new MenuElement(new ItemBuilder(Material.INK_SACK, 1, editing ? (byte) 10 : (byte) 8)
                    .setName(editing ? "&a&lEditing" : "&7Editing")
                    .addLore("&7Click to toggle")
                    .build())
                    .clickBuilder()
                    .onLeftClick((e) -> {
                        editing = !editing;
                        build(player);
                        invalidate();
                    }).build();
            setElement(4, edit);
        }
    }
}
