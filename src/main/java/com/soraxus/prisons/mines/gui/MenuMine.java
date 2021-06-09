package com.soraxus.prisons.mines.gui;

import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MenuMine extends Menu {
    private Mine mine;
    private MenuElement backElement;

    public MenuMine(Mine mine, MenuElement backElement) {
        super(mine.getName(), 5);
        this.mine = mine;
        this.backElement = backElement;
        this.setup();
    }

    public void setup() {
        MenuElement reset = new MenuElement(new ItemBuilder(Material.BEACON, 1).setName("&c&lReset").addLore("&7Click to reset this mine")
                .build()).setClickHandler((e, i) -> mine.reset());

        MenuElement delete = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&4&lDelete")
                .build()).setClickHandler((e, i) -> {
            MineManager.instance.remove(mine.getName());
            backElement.getClickHandler().handleClick(e, i);
        });

        MenuElement blocks = new MenuElement(new ItemBuilder(Material.COAL_BLOCK, 1).setName("&6&lBlocks").build())
                .setClickHandler((e, i) -> new MenuEditBlocks(mine, getBackButton(this).setClickHandler((e1, i1) -> {
                    this.setup();
                    this.open((Player) e1.getWhoClicked());
                })).open((Player) e.getWhoClicked()));

        this.setElement(20, reset);
        this.setElement(22, blocks);
        this.setElement(24, delete);
        this.setElement(4, backElement);

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
