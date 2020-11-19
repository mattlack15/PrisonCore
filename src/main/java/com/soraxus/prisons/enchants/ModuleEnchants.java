package com.soraxus.prisons.enchants;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.cmd.CmdCE;
import com.soraxus.prisons.enchants.gui.MenuEnchant;
import com.soraxus.prisons.enchants.gui.MenuModuleEnchants;
import com.soraxus.prisons.enchants.manager.AbilityCooldownManager;
import com.soraxus.prisons.enchants.manager.EnchantManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.items.NBTUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ModuleEnchants extends CoreModule {
    public static ModuleEnchants instance;

    @Override
    public String getName() {
        return "Custom_Enchantments";
    }

    @Override
    public void onEnable() {
        instance = this;

        new EnchantManager();
        new AbilityCooldownManager();

        new CmdCE().register();

        EnchantManager.instance.loadEnchants("com.soraxus.prisons.enchants.customenchants", this.getClass().getClassLoader());
    }

    public void onDisable() {
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.ENCHANTED_BOOK).setName("&d&lEnchantments")
                .addLore("&7Click to view custom enchantments").build())
                .setClickHandler((e, i) -> new MenuModuleEnchants(backButton).open(e.getWhoClicked()));
    }

    @EventSubscription
    private void onClick(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().getType().toString().contains("PICKAXE") || (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)))
            return;

        event.setCancelled(true);
        new MenuEnchant(event.getPlayer(), event.getItem(), EnchantManager.instance.getEnchantments()).open(event.getPlayer());
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
    }

    @EventSubscription
    private void onDragDrop(InventoryClickEvent e) {
        if (e.getCursor() != null && NBTUtils.instance.hasTag(e.getCursor(), "ce.book.type")) {
            if (e.getCurrentItem() != null) {
                AbstractCE ce = EnchantManager.instance.byBook(e.getCursor());
                if (ce == null || !ce.canEnchantItem(e.getCurrentItem()))
                    return;

                int level = EnchantManager.instance.getLevelByBook(e.getCursor());
                e.getClickedInventory().setItem(e.getSlot(), ce.enchant(e.getCurrentItem(), level));
                e.getWhoClicked().setItemOnCursor(null);
                e.setCancelled(true);
            }
        }
    }
}