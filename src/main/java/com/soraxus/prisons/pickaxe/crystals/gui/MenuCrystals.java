package com.soraxus.prisons.pickaxe.crystals.gui;

import com.soraxus.prisons.enchants.gui.MenuEnchant;
import com.soraxus.prisons.pickaxe.crystals.Crystal;
import com.soraxus.prisons.pickaxe.crystals.CrystalInfo;
import com.soraxus.prisons.pickaxe.crystals.CrystalManager;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Handlers;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MenuCrystals extends Menu {
    private MenuEnchant parent;
    private CrystalInfo info;

    public MenuCrystals(MenuEnchant parent) {
        super("§e§lPickaxe Upgrades", 5);
        this.parent = parent;
    }

    private MenuElement getCrystalItem(int index) {
        if (index >= info.getCrystals().size()) {
            return new MenuElement(new ItemBuilder(Material.BARRIER)
                    .setName("§cError: Invalid index = " + index)
                    .build())
                    .setClickHandler(Handlers.noop);
        }

        if (info.getCrystals().get(index) == null) {
            return new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE).setName("&a&lDrag a Crystal here")
                    .setDurability((short) 5)
                    .build()
            ).setClickHandler((event, i) -> {
                ItemStack cr = event.getCursor(); // TODO: Make sure this actually works
                if (cr == null) {
                    return;
                }
                if (!Crystal.isCrystalItem(cr)) {
                    return;
                }
                Crystal c = Crystal.fromItem(cr);
                c.setIndex(index);
                info.getCrystals().set(index, c);

                //Save to item
                parent.getToEnchant().setItemMeta(CrystalManager.apply(parent.getToEnchant(), info).getItemMeta());

                event.getWhoClicked().setItemOnCursor(null);
                setup();
                open(event.getWhoClicked());
            });
        } else {
            Crystal crystal = info.getCrystals().get(index);
            return new MenuElement(crystal.getItem())
                    .setClickHandler(Handlers.noop);
        }
    }

    private void reloadInfo() {
        this.info = CrystalManager.getInfo(parent.getToEnchant());
        System.out.println("Loaded crystals: " + info.toString());
    }

    public void setup() {
        reloadInfo();
        this.setElement(10, getCrystalItem(0));
        this.setElement(13, getCrystalItem(1));
        this.setElement(16, getCrystalItem(2));
        this.setElement(28, getCrystalItem(3));
        this.setElement(31, getCrystalItem(4));
        this.setElement(34, getCrystalItem(5));

        this.fillElement(getFiller(7));
    }
}
