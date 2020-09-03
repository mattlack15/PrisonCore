package com.soraxus.prisons.crate;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.crate.gui.MenuCrates;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ModuleCrates extends CoreModule {
    public static ModuleCrates instance;

    @Override
    protected void onEnable() {
        instance = this;
        this.createFiles(CrateFiles.class);
        new CrateManager(CrateFiles.CRATES_FILE);
        CrateManager.instance.loadAll();
    }

    @Override
    public String getName() {
        return "Crates";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.ENDER_CHEST, 1).setName("&f&lCrates").addLore("&7Click to manage &f&lCrates").build())
        .setClickHandler((e, i) -> new MenuCrates(backButton).open((Player) e.getWhoClicked()));
    }

    @EventSubscription
    private void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR))
            return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        Crate crate = Crate.getCrate(event.getItem());
        if (crate != null) {
            ItemUtils.decrementHand(event.getPlayer());
            crate.open(event.getPlayer());
            event.setCancelled(true);
        }
    }
}
