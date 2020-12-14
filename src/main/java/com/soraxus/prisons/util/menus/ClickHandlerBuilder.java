package com.soraxus.prisons.util.menus;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ClickHandlerBuilder {
    private boolean cancelClicks = true;
    private Consumer<InventoryClickEvent> onLeftClick = null;
    private Consumer<InventoryClickEvent> onRightClick = null;
    private Consumer<InventoryClickEvent> onShiftLeftClick = null;
    private Consumer<InventoryClickEvent> onShiftRightClick = null;
    private Consumer<InventoryClickEvent> onDropItem = null;
    private Menu openMenu = null;

    private final MenuElement ret;

    public ClickHandlerBuilder(MenuElement ret) {
        this.ret = ret;
    }

    public ClickHandlerBuilder cancelClicks(boolean value) {
        cancelClicks = value;
        return this;
    }

    public ClickHandlerBuilder openMenu(Menu menu) {
        this.openMenu = menu;
        return this;
    }

    public ClickHandlerBuilder onLeftClick(Consumer<InventoryClickEvent> runnable) {
        onLeftClick = runnable;
        return this;
    }

    public ClickHandlerBuilder onRightClick(Consumer<InventoryClickEvent> runnable) {
        onRightClick = runnable;
        return this;
    }

    public ClickHandlerBuilder onShiftLeftClick(Consumer<InventoryClickEvent> runnable) {
        onShiftLeftClick = runnable;
        return this;
    }

    public ClickHandlerBuilder onShiftRightClick(Consumer<InventoryClickEvent> runnable) {
        onShiftRightClick = runnable;
        return this;
    }

    public ClickHandlerBuilder onDropItem(Consumer<InventoryClickEvent> runnable) {
        onDropItem = runnable;
        return this;
    }

    public MenuElement build() {
        ret.setClickHandler((e, i) -> {
            if (cancelClicks)
                e.setCancelled(true);

            boolean canOpenMenu = true;

            if (e.getClick().isShiftClick()) {
                if (e.getClick().isLeftClick()) {
                    if (onShiftLeftClick != null) {
                        onShiftLeftClick.accept(e);
                        canOpenMenu = false;
                    }
                } else if (e.getClick().isRightClick()) {
                    if (onShiftRightClick != null) {
                        onShiftRightClick.accept(e);
                        canOpenMenu = false;
                    }
                }
            } else {
                if (e.getClick().isLeftClick()) {
                    if (onLeftClick != null) {
                        onLeftClick.accept(e);
                        canOpenMenu = false;
                    }
                } else if (e.getClick().isRightClick()) {
                    if (onRightClick != null) {
                        onRightClick.accept(e);
                        canOpenMenu = false;
                    }
                } else if (e.getClick() == ClickType.DROP) {
                    if (onDropItem != null) {
                        onDropItem.accept(e);
                        canOpenMenu = false;
                    }
                }
            }
            if (openMenu != null && canOpenMenu)
                openMenu.open(e.getWhoClicked());
        });
        return ret;
    }
}
