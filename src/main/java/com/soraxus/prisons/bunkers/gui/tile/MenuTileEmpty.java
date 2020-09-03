package com.soraxus.prisons.bunkers.gui.tile;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class MenuTileEmpty extends Menu {
    private final IntVector2D tile;
    private final Bunker bunker;
    public MenuTileEmpty(Bunker bunker, IntVector2D tile) {
        super("Empty Tile", 3);
        this.tile = tile;
        this.bunker = bunker;
        this.setup();
    }

    public void setup() {

        this.setAll(null);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        //Build element
        Worker availableWorker = bunker.getFreeWorker(); //Null if none
        ItemBuilder builder = new ItemBuilder(Material.IRON_AXE, 1).addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if(availableWorker != null) {
            builder.setName("&6&lBuild");
            builder.addLore("&7Click to build something");
            builder.addLore("&7new and exciting!");
        } else {
            builder.setName("&4&lBuild");
            builder.addLore("&cYou've run out of workers!");
            builder.addLore("&cIt is not good to work them till they die!");
        }
        builder.addLore("");
        builder.addLore("&fAvailable Workers: " + (int) bunker.getWorkers().stream().filter((w) -> !w.isWorking()).count());
        MenuElement build = new MenuElement(builder.build()).setClickHandler((e, i) -> {
            Worker worker = bunker.getFreeWorker();
            if(worker == null) {
                setup();
                getElement(e.getSlot()).addTempLore(this, "&cInsufficient Workers!", 60);
                return;
            }
            bunker.getElementShop(tile).getMenu(getBackButton(this)).open(e.getWhoClicked());
        });

        this.setElement(11, build);
    }
}
