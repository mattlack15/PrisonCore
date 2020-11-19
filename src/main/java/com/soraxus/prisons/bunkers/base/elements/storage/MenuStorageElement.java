package com.soraxus.prisons.bunkers.base.elements.storage;

import com.soraxus.prisons.bunkers.workers.TaskBreak;
import com.soraxus.prisons.bunkers.workers.TaskUpgrade;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.string.TextUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class MenuStorageElement extends Menu {
    private final StorageElement element;

    public MenuStorageElement(StorageElement element) {
        super(element.getName() + " lvl " + element.getLevel(), 3);
        this.element = element;
        this.setup();
    }

    public void setup() {

        this.setAll(null);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        MenuElement removeElement = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&c&lRemove")
                .addLore("&7Click to start removing this element").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build()).setClickHandler((e, i) -> {
            Worker worker = element.getBunker().getFreeWorker();
            if (worker == null) {
                getElement(e.getSlot()).addTempLore(this, "&cNo available worker!", 60);
                return;
            }
            TaskBreak task = new TaskBreak(element.getBunker().getTileMap().getTile(element.getPosition()), worker);
            task.start();
            e.getWhoClicked().closeInventory();
        });

        ItemBuilder builder = new ItemBuilder(Material.CHEST, 1).setName("&aCurrent Storage").addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        for (Storage storage : element.getStorageList()) {
            builder.addLore(storage.getResource().getColor() + storage.getResource().getDisplayName() + " &f: &7" + MathUtils.round(storage.getAmount(), 1) + "/" + storage.getCap());
            builder.addLore("&8[" + TextUtil.generateBar('2', 'f', 28, (int) storage.getAmount(), (int) storage.getCap()) + "&8]");
        }
        MenuElement currentStorage = new MenuElement(builder.build());

        builder = new ItemBuilder(Material.ANVIL, 1).setName("&6&lUpgrade").addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        builder.addLore("&7Click to upgrade this");
        boolean a = true;
        for (Storage storage : element.getType().getBuildCost(element.getLevel() + 1)) {
            if (a) {
                builder.addLore("");
                a = false;
            }
            builder.addLore(storage.getResource().getColor() + storage.getResource().getDisplayName() + " &f" + storage.getAmount());
        }
        if (element.getLevel() >= element.getMaxLevel()) {
            builder.addLore("");
            builder.addLore("&cThis building has reached the max level!");
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
            e.getWhoClicked().closeInventory();
        });
        this.setElement(11, upgrade);
        this.setElement(13, currentStorage);
        this.setElement(4, element.getInfoElement());
        if (element.isRemovable())
            this.setElement(15, removeElement);
    }
}
