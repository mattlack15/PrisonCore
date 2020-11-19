package com.soraxus.prisons.privatemines.gui;

import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.VisitationType;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MenuPrivateMine extends Menu {
    private PrivateMine mine;

    public MenuPrivateMine(PrivateMine mine) {
        super("§d" + mine.getGang().getName(), 3);
        this.mine = mine;
        this.setup();
    }

    public void setup() {
        MenuElement upgrade = new MenuElement(
                new ItemBuilder(Material.BEACON)
                        .setName("§5Upgrade")
                        .addLore("&8Current Level: &f" + mine.getRank(),
                                "",
                                "§fClick to upgrade your Gang Mine!",
                                "",
                                "§fPrice: §dTODO"
                        )
                        .build()
        );
        upgrade.setClickHandler((e, i) -> {
            mine.upgrade();
            mine.reset();
            this.setup();
        });

        MenuElement slots = new MenuElement(
                new ItemBuilder(Material.ITEM_FRAME)
                        .setName("§5Slots")
                        .addLore(
                                "§fClick here to manage the slots",
                                "§fof this Gang Mine!"
                        )
                        .build()
        );
        slots.setClickHandler((e, i) -> new MenuPrivateMineSlots(mine, getBackButton(this))
                .open(e.getWhoClicked()));

        MenuElement teleport = new MenuElement(
                new ItemBuilder(Material.EYE_OF_ENDER).setName("&5Teleport")
                        .addLore("&fClick to teleport to your mine").build()).setClickHandler((e, i) -> {
            if (!mine.getVisitationManager().addVisitor(e.getWhoClicked().getUniqueId(), VisitationType.FREE)) {
                getElement(e.getSlot()).addTempLore(this, "&cNo more space for you :(", 40);
            }
            e.getWhoClicked().closeInventory();
            mine.teleport((Player) e.getWhoClicked());
        });

        this.setElement(11, slots);
        this.setElement(13, teleport);
        this.setElement(15, upgrade);
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
