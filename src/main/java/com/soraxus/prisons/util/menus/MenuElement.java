package com.soraxus.prisons.util.menus;

import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.Scheduler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuElement {
    private boolean staticItem = true;
    private ItemStack stack;
    private int updateEvery = 2;
    private boolean doUpdates;
    private ClickHandler clickHandler = null;
    private UpdateHandler updateHandler = null;


    public MenuElement(ItemStack stack) {
        this.stack = stack;
    }

    //Getters

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public MenuElement setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
        return this;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public MenuElement setClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    //Util
    public MenuElement setDoUpdates(boolean doUpdates) {
        this.doUpdates = doUpdates;
        return this;
    }

    /**
     * Adds a temporary lore
     *
     * @param menu  The menu this element is assigned to
     * @param lore  The lore to add
     * @param ticks The length of time in ticks (20ths of a second) that it will stay in effect
     */
    public void addTempLore(Menu menu, String lore, int ticks) {
        int index = menu.indexOfElement(this);
        if (index == -1 || this.getItem() == null) {
            return;
        }
        this.setItem(new ItemBuilder(this.getItem()).addLore(lore).build());
        MenuManager.instance.invalidateElementsInInvForMenu(menu, index);
        Scheduler.scheduleSyncDelayedTaskT(() -> {
            MenuElement.this.setItem(new ItemBuilder(MenuElement.this.getItem()).removeLore(lore, false).build());
            MenuManager.instance.invalidateElementsInInvForMenu(menu, index);
        }, ticks);
    }

    public int getUpdateEvery() {
        return updateEvery;
    }

    public MenuElement setUpdateEvery(int updateEvery) {
        this.setDoUpdates(true);
        this.updateEvery = updateEvery;
        return this;
    }

    public boolean isDoingUpdates() {
        return doUpdates;
    }

    public boolean isStaticItem() {
        return staticItem;
    }

    public MenuElement setStaticItem(boolean staticItem) {
        this.staticItem = staticItem;
        return this;
    }

    public ItemStack getItem() {
        return stack;
    }

    public MenuElement setItem(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    //Extra classes and interfaces

    public interface UpdateHandler {
        void handleUpdate(MenuElement element);
    }

    public interface ClickHandler {
        void handleClick(InventoryClickEvent event, InvInfo info);
    }
}
