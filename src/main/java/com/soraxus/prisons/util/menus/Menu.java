package com.soraxus.prisons.util.menus;

import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class Menu {

    public static final Material BACK_BUTTON_ITEM = Material.BED;
    public static final Material PAGE_CONTROL_ITEM = Material.ARROW;
    //
    //Storage vars
    private final Map<Integer, MenuElement> elements = new HashMap<>();
    //Value vars
    private String title;
    private com.soraxus.prisons.util.menus.MenuElement.ClickHandler defaultClickHandler = null;

    //Construction
    public Menu(String title, int rows) {
        this.title = title;
        this.setSize(rows);
    }

    public static MenuElement getBackButton(MenuElement.ClickHandler handler) {
        MenuElement element = new MenuElement(new ItemBuilder(BACK_BUTTON_ITEM, 1).setName("&b&lBack").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
        element.setClickHandler(handler).setStaticItem(true);
        return element;
    }

    public static MenuElement getBackButton(Menu backMenu) {
        if (backMenu == null) {
            return getBackButton((e, i) -> {
            });
        }
        return getBackButton((e, i) -> backMenu.open(e.getWhoClicked()));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Inventory buildInventory() {
        Inventory inv = Bukkit.createInventory(null, this.getSize(), title);
        for (int i = 0; i < this.getSize(); i++) {
            MenuElement e = this.getElement(i);
            if (e == null) {
                continue;
            }
            inv.setItem(i, e.getItem());
        }
        return inv;
    }

    public MenuElement.ClickHandler getDefaultClickHandler() {
        return defaultClickHandler;
    }

    public void setDefaultClickHandler(MenuElement.ClickHandler handler) {
        this.defaultClickHandler = handler;
    }

    //open
    public void open(HumanEntity p, Object... data) {
        if (p == null)
            return;
        Inventory inv = this.buildInventory();
        InvInfo info = new InvInfo(inv, this, data);

        InvInfo pastInfo = MenuManager.instance.getInfo(p.getUniqueId());

        //For immediate effect
        MenuManager.instance.addMenu(this); //Make sure this menu is added to the list
        MenuManager.instance.setInfo(p.getUniqueId(), info);
        Synchronizer.synchronize(() -> {
            //To make sure it is set when the inv is opened
            if (pastInfo != null && pastInfo.getCurrentInv() != null && pastInfo.getCurrentInv().getSize() == inv.getSize() && pastInfo.getCurrentInv().getTitle().equals(inv.getTitle())) {
                pastInfo.getCurrentInv().setContents(inv.getContents());
                info.setCurrentInv(pastInfo.getCurrentInv());
                MenuManager.instance.addMenu(this); //Make sure this menu is added to the list
                MenuManager.instance.setInfo(p.getUniqueId(), info);
            } else {
                p.openInventory(inv);
                MenuManager.instance.addMenu(this); //Make sure this menu is added to the list
                MenuManager.instance.setInfo(p.getUniqueId(), info);
            }
        });
    }

    public int indexOfElement(@NotNull MenuElement e) {
        for (int i = 0; i < this.elements.size(); i++) {
            if (e.equals(this.getElement(i))) {
                return i;
            }
        }
        return -1;
    }

//    public static CompletableFuture<Void> openMenuSync(@NotNull Player player, @NotNull Menu menu, Object... data){
//        CompletableFuture<Void> future = new CompletableFuture<>();
//        if(Bukkit.isPrimaryThread()){
//            menu.open(player, data);
//            future.complete(null);
//        } else {
//            ModuleEnchants.instance.getImpl().getSyncExecutor().execute(() -> {
//                menu.open(player, data);
//                future.complete(null);
//            });
//        }
//        return future;
//    }

    //Util
    public int getSize() {
        return this.elements.size();
    }

    public void setSize(int rows) {
        int size = rows * 9;
        if (elements.size() > size) {
            for (int i = elements.size() - 1; i != size - 1; i--) { // for -1 change if doesn't work but you did this briefly in your head (approx. 3m of thinking)
                elements.remove(i);
            }
        } else if (elements.size() < size) {
            for (int i = elements.size(); i != size; i++) {
                elements.put(i, null);
            }
        }
        if (size != this.elements.size()) {
            System.out.println("Problem making size adjustment in Menus -> Menu -> setSize(int size)");
        }
    }

    //Element management

    public int getRows() {
        return this.elements.size() / 9;
    }

    public void setElement(int position, MenuElement e) {
        if (position >= this.elements.size()) {
            return;
        }
        this.elements.put(position, e);
    }

    public MenuElement getElement(int slot) {
        return this.elements.get(slot);
    }

    public void fillElement(MenuElement e) {
        for (int i = 0; i < elements.size(); i++) {
            if (this.getElement(i) == null) {
                this.setElement(i, e);
            }
        }
    }

    public void setRow(int row, MenuElement e) {

        if (row * 9 + 9 > this.elements.size()) {
            return;
        }

        int startingPos = row * 9;

        for (int i = startingPos; i < startingPos + 9; i++) {
            this.setElement(i, e);
        }

    }

    public void setAll(MenuElement e) {
        for (int i = 0; i < elements.size(); i++) {
            this.setElement(i, e);
        }
    }

    public void clear() {
        setAll(null);
    }

    public Map<Integer, MenuElement> getElements() {
        return elements;
    }

    public AtomicInteger setupActionableList(int startPos, int endPos, int backPos, int nextPos, Function<Integer, MenuElement> elementSupplier, int page) {
        return setupActionableList(startPos, endPos, backPos, nextPos, elementSupplier, new AtomicInteger(page));
    }

    public AtomicInteger setupActionableList(int startPos, int endPos, int backPos, int nextPos, Function<Integer, MenuElement> elementSupplier, AtomicInteger pageHandler) {
        //Pageable list
        int page = pageHandler.get();

        int calculatedMarginLeft = startPos % 9;
        int calculatedMarginRight = 8 - endPos % 9;

        int elementIndex = page * (9 - calculatedMarginLeft - calculatedMarginRight) * (((endPos - (endPos % 9)) / 9) - ((startPos - (startPos % 9)) / 9) + 1);
        boolean placing = true;
        for (int slot = startPos; slot <= endPos; slot++) {

            if (placing) {
                MenuElement element = elementSupplier.apply(elementIndex);
                if (element == null) {
                    placing = false;
                    this.setElement(slot, null);
                } else {
                    this.setElement(slot, element);
                }
            } else {
                this.setElement(slot, null);
            }

            if (8 - slot % 9 <= calculatedMarginRight) {
                slot += calculatedMarginLeft + calculatedMarginRight;
            }

            elementIndex++;
        }

        MenuElement back = new MenuElement(new ItemBuilder(Material.ARROW, 1).setName("&fBack").build()).setClickHandler((e, i) -> {
            pageHandler.decrementAndGet();
            this.setupActionableList(startPos, endPos, backPos
                    , nextPos, elementSupplier, pageHandler);
        });
        MenuElement next = new MenuElement(new ItemBuilder(Material.ARROW, 1).setName("&fNext").build()).setClickHandler((e, i) -> {
            pageHandler.incrementAndGet();
            this.setupActionableList(startPos, endPos, backPos
                    , nextPos, elementSupplier, pageHandler);
        });

        if (page != 0) {
            this.setElement(backPos, back);
        } else {
            this.setElement(backPos, null);
        }
        if (elementSupplier.apply(elementIndex) != null) {
            this.setElement(nextPos, next);
        } else {
            this.setElement(nextPos, null);
        }
        MenuManager.instance.invalidateInvsForMenu(this);
        return pageHandler;
    }

    protected MenuElement getFiller(int dat) {
        return new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) dat)
                .setName("§7")
                .build()
        ).setClickHandler(Handlers.noop);
    }

}
