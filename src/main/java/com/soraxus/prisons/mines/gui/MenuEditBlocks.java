package com.soraxus.prisons.mines.gui;

import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.InvInfo;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuEditBlocks extends Menu {

    private Mine mine;
    private MenuElement backElement;

    public MenuEditBlocks(Mine mine, MenuElement backElement) {
        super("Blocks for " + mine.getName(), 5);
        this.mine = mine;
        this.backElement = backElement;
        this.setup();
        EventSubscriptions.instance.subscribe(this);
    }

    @EventSubscription(priority = EventPriority.LOW)
    private void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        InvInfo info = MenuManager.instance.getInfo(event.getWhoClicked().getUniqueId());
        if (info.getCurrentInv() == null)
            return;
        if (view != null && view.getTopInventory() != null && view.getTopInventory().equals(info.getCurrentInv())) {

            ItemStack stack = event.getCurrentItem();
            if (stack != null && stack.getType() != Material.AIR && stack.getType().isBlock()) {

                if (event.getClickedInventory().equals(info.getCurrentInv()))
                    return;

                if (event.getView().getTopInventory().equals(event.getClickedInventory()) || event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                    event.setCancelled(true);
                    mine.addMineBlock(stack.getData().getData() << 12 | stack.getTypeId(), 0.5d);
                    this.setup();
                }
            }
        }
    }

    public void setup() {
        this.setAll(null);
        Map<Integer, Double> blocks = mine.getBlocks();
        List<Integer> blockIds = new ArrayList<>(blocks.keySet());

        this.setElement(6, new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&a&lAdd")
                .addLore("&7To add, drag an displayItem from your inventory into this menu").build()));

        this.setElement(4, backElement);

        this.setupActionableList(10, 9 * 4 - 2, 9 * 4, 9 * 5 - 1, (index) -> {
            if (index >= blockIds.size()) {
                return new MenuElement(null).setClickHandler((e, i) -> {
                    e.setCancelled(false);
                });
            } else {
                Material mat = Material.getMaterial(blockIds.get(index) & 4095);
                if (mat == Material.AIR)
                    return new MenuElement(null);
                byte data = (byte) ((blockIds.get(index) >> 12) & 255);

                return new MenuElement(new ItemBuilder(mat, 1, data).addLore("&fCurrent Chance: &7" + (blocks.get(blockIds.get(index)) * 100) + "%")
                        .addLore("&eLeft/Right Click - Raise/Lower percentage").addLore("&7Shift-Right Click to Remove").build()).setClickHandler((e, i) -> {
                    if (e.getClick().isRightClick() && e.getClick().isShiftClick()) {
                        mine.removeMineBlock(blockIds.get(index));
                    } else if (e.getClick().isLeftClick()) {
                        mine.addMineBlock(blockIds.get(index), blocks.get(blockIds.get(index)) + 0.05d);
                    } else if (e.getClick().isRightClick()) {
                        mine.addMineBlock(blockIds.get(index), blocks.get(blockIds.get(index)) - 0.05d);
                    }
                    this.setup();
                });
            }
        }, 0);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build()));
        MenuManager.instance.invalidateInvsForMenu(this);
    }

}
