package com.soraxus.prisons.bunkers.gui.tile;

import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.workers.TaskBreak;
import com.soraxus.prisons.bunkers.workers.TaskUpgrade;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class MenuTileOccupied extends Menu {
    private final Tile tile;

    public MenuTileOccupied(Tile tile) {
        super(tile.getParent().getName(), 3);
        this.tile = tile;
        this.setup();
    }

    public void setup() {

        this.setAll(null);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        BunkerElement element = tile.getParent();
        MenuElement removeElement = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&c&lRemove")
                .addLore("&7Click to start removing this element").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build()).setClickHandler((e, i) -> {
            Worker worker = element.getBunker().getFreeWorker();
            if (worker == null) {
                getElement(e.getSlot()).addTempLore(this, "&cNo available worker!", 60);
                return;
            }
            TaskBreak task = new TaskBreak(element.getBunker().getTileMap().getTile(element.getPosition()), worker);
            task.start();
            e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.8f, 1.1f);
            e.getWhoClicked().closeInventory();
        });
        ItemBuilder builder = new ItemBuilder(Material.ANVIL, 1).setName("&6&lUpgrade");
        builder.addLore("&7Click to upgrade this").addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        boolean a = true;
        for (Storage storage : element.getType().getBuildCost(element.getLevel() + 1)) {
            if (a) {
                builder.addLore("");
                a = false;
            }
            builder.addLore(storage.getResource().getColor() + storage.getResource().getDisplayName() + " &f" + MathUtils.round(storage.getAmount(), 1));
        }
        if (element.getLevel() >= element.getMaxLevel()) {
            builder.addLore("");
            builder.addLore("&cThis element has reached the max level!");
        }
        if (!element.getBunker().hasResources(element.getType().getBuildCost(element.getLevel() + 1))) {
            builder.addLore("");
            builder.addLore("&cYou do not have enough resources to upgrade!");
        }
        if (element.getBunker().getFreeWorker() == null) {
            builder.addLore("");
            builder.addLore("&cYou have no free workers!");
        }
        MenuElement upgrade = new MenuElement(builder.build()).setClickHandler((e, i) -> {
            if (!element.getBunker().hasResources(element.getType().getBuildCost(element.getLevel() + 1))) {
                getElement(e.getSlot()).addTempLore(this, "&cInsufficient resources!", 60);
                return;
            }
            Worker worker = element.getBunker().getFreeWorker();
            if (worker == null) {
                getElement(e.getSlot()).addTempLore(this, "&cInsufficient workers!", 60);
                return;
            }
            if (element.getLevel() >= element.getMaxLevel()) {
                getElement(e.getSlot()).addTempLore(this, "&cMax level reached!", 60);
                return;
            }
            TaskUpgrade taskUpgrade = new TaskUpgrade(element.getBunker().getTileMap().getTile(element.getPosition()), worker);
            taskUpgrade.start();
            e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_GENERIC_SMALL_FALL, 1f, 1.1f);
            e.getWhoClicked().closeInventory();
        });

        this.setElement(element.isRemovable() ? 11 : 13, upgrade);
        this.setElement(4, element.getInfoElement());
        if (element.isRemovable())
            this.setElement(15, removeElement);

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
