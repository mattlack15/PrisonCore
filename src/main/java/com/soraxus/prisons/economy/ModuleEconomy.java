package com.soraxus.prisons.economy;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;

public class ModuleEconomy extends CoreModule {
    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.DOUBLE_PLANT).setName("&f&lEconomies").addLore("&7Click to view &f&lEconomies")
        .build()).setClickHandler((e, i) -> new MenuModuleEconomy(backButton).open(e.getWhoClicked()));
    }
}
