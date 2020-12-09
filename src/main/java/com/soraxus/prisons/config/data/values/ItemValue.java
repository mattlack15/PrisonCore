package com.soraxus.prisons.config.data.values;

import com.soraxus.prisons.config.data.ConfigValue;
import com.soraxus.prisons.pickaxe.crystals.Crystal;
import com.soraxus.prisons.pickaxe.crystals.CrystalManager;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemValue extends ConfigValue<ItemStack> {
    public ItemValue(String name, String[] description, ItemStack def) {
        super(name, description, def);
    }

    @Override
    public MenuElement createElement() {
        return new MenuElement(getValue()).setClickHandler((event, i) -> {
            ItemStack stack = event.getCursor();
            if (stack == null) {
                return;
            }
            setValue(stack);
            i.getCurrentMenu().open(event.getWhoClicked());
        });
    }
}
