package com.soraxus.prisons.cells.minions;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MenuMinion extends Menu {

    private final Minion minion;

    public MenuMinion(Minion minion) {
        super("Minion", 3);
        this.minion = minion;
        this.setup();
    }

    public void setup() {
        MenuElement upgradeSpeed = new MenuElement(new ItemBuilder(Material.FEATHER).setName("&6&lUpgrade Speed")
                .addLore("&fCurrent: &7" + Math.round(minion.getSpeed()))
                .addLore("&fUpgrade Cost: &e$" + NumberUtils.toReadableNumber(MinionUpgrades.getPrice((int) minion.getSpeed())))
                .addLore("", "&8Click to upgrade").build())
                .setClickHandler((e, i) -> {
                    double current = minion.getSpeed();
                    long price = MinionUpgrades.getPrice((int) current);
                    if (!Economy.money.hasBalance(e.getWhoClicked().getUniqueId(), price)) {
                        this.getElement(e.getSlot()).addTempLore(this, "&cYou're too poor :.(", 60);
                        return;
                    }
                    minion.setSpeed(current + 1D);
                    Economy.money.removeBalance(e.getWhoClicked().getUniqueId(), price);
                    setup();
                });

        MenuElement collect = new MenuElement(new ItemBuilder(Material.EXP_BOTTLE)
                .setName("&6&lCollect")
                .addLore("&fStored: &a" + minion.getStored().get())
                .addLore("", "&8Click to collect").build()).setClickHandler((e, i) -> {

            if (!e.getWhoClicked().getUniqueId().equals(minion.getCreator()))
                return;

            int give = minion.getStored().getAndSet(0);
            Economy.tokens.addBalance(e.getWhoClicked().getUniqueId(), give);
            setup();
        }).setDoUpdates(true).setUpdateEvery(1).setUpdateHandler((e) -> e.setItem(new ItemBuilder(Material.EXP_BOTTLE)
                .setName("&6&lCollect")
                .addLore("&fStored Tokens: &a" + minion.getStored().get())
                .addLore("", "&8Click to collect").build()));

        MenuElement pickup = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK)
        .setName("&cPickup").addLore("&7Click this to remove and pickup this minion").build()).setClickHandler((e, i) -> {
            this.minion.remove();
            e.getWhoClicked().getInventory().addItem(this.minion.asItemStack());
            ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            e.getWhoClicked().closeInventory();
        });

        this.setElement(11, upgradeSpeed);
        this.setElement(13, collect);
        this.setElement(15, pickup);

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
