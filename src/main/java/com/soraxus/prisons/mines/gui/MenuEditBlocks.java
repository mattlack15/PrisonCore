package com.soraxus.prisons.mines.gui;

import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
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
import java.util.concurrent.atomic.AtomicLong;

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

    @EventSubscription(
            priority = EventPriority.LOW
    )
    private void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        InvInfo info = MenuManager.instance.getInfo(event.getWhoClicked().getUniqueId());
        if (info.getCurrentInv() != null) {
            if (view != null && view.getTopInventory() != null && view.getTopInventory().equals(info.getCurrentInv())) {
                ItemStack stack = event.getCurrentItem();
                if (stack != null && stack.getType() != Material.AIR && stack.getType().isBlock()) {
                    if (event.getClickedInventory().equals(info.getCurrentInv())) {
                        return;
                    }

                    if (event.getView().getTopInventory().equals(event.getClickedInventory()) || event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                        event.setCancelled(true);
                        this.mine.addMineBlock(stack.getData().getData() << 12 | stack.getTypeId(), 0.5D);
                        MineManager.instance.queueSaveMineOperation(this.mine);
                        this.setup();
                    }
                }
            }

        }
    }

    public void setup() {
        this.setAll((MenuElement)null);
        Map<Integer, Double> blocks = this.mine.getBlocks();
        List<Integer> blockIds = new ArrayList<>(blocks.keySet());
        this.setElement(6, new MenuElement((new ItemBuilder(Material.ANVIL, 1)).setName("&a&lAdd").addLore("&7To add, drag an displayItem from your inventory into this menu").build()));
        this.setElement(4, this.backElement);
        this.setupActionableList(10, 34, 36, 44, (index) -> {
            if (index >= blockIds.size()) {
                return (new MenuElement(null)).setClickHandler((e, i) -> e.setCancelled(false));
            } else {
                Material mat = Material.getMaterial(blockIds.get(index) & 4095);
                if (mat == Material.AIR) {
                    return new MenuElement(null);
                } else {
                    byte data = (byte)(blockIds.get(index) >> 12 & 255);
                    AtomicLong lastClick = new AtomicLong(System.currentTimeMillis());
                    return (new MenuElement((new ItemBuilder(mat, 1, data)).addLore("&fCurrent Chance: &7" + Math.round(blocks.get(blockIds.get(index)) * 100.0D) + "%").addLore("&eLeft/Right Click - Raise/Lower percentage").addLore("&7Shift-Right Click to Remove").build())).setClickHandler((e, i) -> {
                        double moveAmount = 0.01D;
                        if (System.currentTimeMillis() - lastClick.getAndSet(System.currentTimeMillis()) < 200L) {
                            moveAmount = 0.05D;
                        }

                        if (e.getClick().isRightClick() && e.getClick().isShiftClick()) {
                            this.mine.removeMineBlock(blockIds.get(index));
                        } else if (e.getClick().isLeftClick()) {
                            this.mine.addMineBlock(blockIds.get(index), MathUtils.round(blocks.get(blockIds.get(index)) + moveAmount, 2));
                        } else if (e.getClick().isRightClick()) {
                            this.mine.addMineBlock(blockIds.get(index), MathUtils.round(blocks.get(blockIds.get(index)) - moveAmount, 2));
                        }

                        MineManager.instance.queueSaveMineOperation(this.mine);
                        this.setup();
                    });
                }
            }
        }, 0);
        this.fillElement(new MenuElement((new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte)7)).setName(" ").build()));
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}