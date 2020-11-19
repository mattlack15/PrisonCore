package com.soraxus.prisons.enchants.gui;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.manager.EnchantManager;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;

import java.util.List;

public class MenuModuleEnchants extends Menu {
    private final MenuElement back;

    public MenuModuleEnchants(MenuElement back) {
        super("Custom Enchantments", 5);
        this.back = back;
        this.setup();
    }

    public void setup() {
        MenuElement stats = new MenuElement(new ItemBuilder(Material.BOOK).setName("&c&lStats")
                .addLore("&8Loaded enchants: &f" + EnchantManager.instance.getEnchantments().size(),
                        "&8Enabled enchants: &f" + EnchantManager.instance.getEnchantments().stream()
                                .filter(AbstractCE::isEnabled).count()).build());


        List<AbstractCE> enchants = EnchantManager.instance.getEnchantments();
        this.setupActionableList(19, 19 + 7 + 8, 19 + 7 + 8 + 2, 19 + 7 + 8 + 2 + 8,
                (index) -> {
                    if (index >= enchants.size())
                        return null;
                    AbstractCE ench = enchants.get(index);
                    MenuElement element = new MenuElement(new ItemBuilder(Material.ENCHANTED_BOOK)
                            .setName(ench.getDisplayName()).addLore("&8Currently: " + (ench.isEnabled() ? "&aEnabled" : "&cDisabled"),
                                    "", "&8Click to enable/disable").build()).setClickHandler((e, i) -> {
                        if (ench.isEnabled())
                            ench.disable();
                        else
                            ench.enable();
                        setup();
                    });
                    return element;
                }, 0);

        this.setElement(4, stats);
        this.setElement(0, back);
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
