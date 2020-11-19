package com.soraxus.prisons.cells.panel;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MenuCellPanel extends Menu {

    private final Cell parent;

    public MenuCellPanel(Cell parent) {
        super("Your Cell", 3);
        this.parent = parent;
        this.setup();
    }

    public void setup() {
        MenuElement infoElement = new MenuElement(new ItemBuilder(Material.EMPTY_MAP, 1)
                .setName("&a&lInfo").addLore("&8TODO figure out what to put here").build());

        MenuElement tpElement = new MenuElement(new ItemBuilder(Material.ENDER_PEARL, 1)
                .setName("&a&lTeleport").addLore("&7Click to teleport into your cell").build()).setClickHandler((e, i) -> {
            ((Player) e.getWhoClicked()).performCommand("cell tp");
            e.getWhoClicked().closeInventory();
        });

        MenuElement settingsElement = new MenuElement(new ItemBuilder(Material.COMMAND, 1).setName("&a&lSettings")
                .addLore("&7Click to edit your cell's settings").build())
                .setClickHandler((e, i) -> new MenuCellSettings(parent, getBackButton(this)).open(e.getWhoClicked()));

        this.setElement(11, infoElement);
        this.setElement(13, tpElement);
        this.setElement(15, settingsElement);

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
