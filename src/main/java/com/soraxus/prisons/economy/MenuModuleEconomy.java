package com.soraxus.prisons.economy;

import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;

public class MenuModuleEconomy extends Menu {
    private final MenuElement backButton;

    public MenuModuleEconomy(MenuElement backButton) {
        super("Economy", 3);
        this.backButton = backButton;
        this.setup();
    }

    public void setup() {
        MenuElement econMoney = new MenuElement(new ItemBuilder(Material.DOUBLE_PLANT).setName("&aMoney")
        .addLore("&8Default Balance: " + Economy.money.getDefaultBalance()).build());
        MenuElement econTokens = new MenuElement(new ItemBuilder(Material.MELON).setName("&eTokens")
        .addLore("&8Default Balance: " + Economy.tokens.getDefaultBalance()).build());
        MenuElement econStars = new MenuElement(new ItemBuilder(Material.NETHER_STAR).setName("&fStars")
        .addLore("&8Default Balance: " + Economy.stars.getDefaultBalance()).build());

        this.setElement(0, backButton);
        this.setElement(10, econMoney);
        this.setElement(13, econStars);
        this.setElement(16, econTokens);
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
