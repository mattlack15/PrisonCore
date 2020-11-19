package com.soraxus.prisons.cells.shop;

import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;

import java.util.*;

public class MenuCustomShop extends Menu {

    private static final Map<Long, String> updateAmountNames = new HashMap<>();
    private static final List<Long> updateAmounts = new ArrayList<>(updateAmountNames.keySet());
    private static final long maxPrice = 1000000000000000000L;

    static {
        updateAmountNames.put(1L, "$1");
        updateAmountNames.put(10L, "$10");
        updateAmountNames.put(100L, "$100");
        updateAmountNames.put(1000L, "$1K");
        updateAmountNames.put(10000L, "$10K");
        updateAmountNames.put(100000L, "$100K");
        updateAmountNames.put(1000000L, "$1M");
        updateAmountNames.put(10000000L, "$10M");
        updateAmountNames.put(100000000L, "$100M");
        updateAmountNames.put(1000000000L, "$1B");
        updateAmountNames.put(10000000000L, "$10B");
        updateAmountNames.put(100000000000L, "$100B");
        updateAmountNames.put(1000000000000L, "$1T");
        updateAmountNames.put(10000000000000L, "$10T");
        updateAmountNames.put(100000000000000L, "$100T");
        updateAmountNames.put(1000000000000000L, "$1q");
        updateAmountNames.put(10000000000000000L, "$10q");

        updateAmounts.sort(Comparator.comparingLong((l) -> l));
    }

    private final CustomShop shop;
    private final boolean canEdit;
    private int editAmount = 0;

    public MenuCustomShop(CustomShop shop, boolean canEdit) {
        super(shop.getName(), 3);
        this.shop = shop;
        this.canEdit = canEdit;
        this.setupSelect();
    }

    public void setupSelect() {
        this.setSize(3);
        this.setAll(null);
        MenuElement buy = new MenuElement(new ItemBuilder(Material.DOUBLE_PLANT).setName("&aBuy Shop").build())
                .setClickHandler((e, i) -> this.setupBuy());

        MenuElement sell = new MenuElement(new ItemBuilder(Material.EMERALD).setName("&cSell Shop").build())
                .setClickHandler((e, i) -> this.setupSell());

        this.setElement(12, buy);
        this.setElement(15, sell);
        MenuManager.instance.invalidateInvsForMenu(this);
    }

    public void setupBuy() {
        this.setSize(6);
        this.setAll(null);

        MenuElement back = new MenuElement(new ItemBuilder(Material.BED).setName("&6Back").build()).setClickHandler((e, i) -> setupSelect());
        this.setElement(4, back);

        List<ShopItem> items = shop.getItems().copy();
        items.removeIf(i -> !i.getType().equals(ShopItem.ShopItemType.BUY));

        this.setupActionableList(9, 44, 45, 45 + 8, (index) -> {
            if(index > items.size()) {
                return null;
            } else if(index == items.size()) {
                //Placeholder

                return null;
            } else {
                //Item
                ShopItem item = items.get(index);
                ItemBuilder builder = new ItemBuilder(item.getItem());

                if(!canEdit) {
                    builder.addLore("", "&aClick to buy for &f" + NumberUtils.toReadableNumber(item.getCost()));
                } else {
                    builder.addLore("", "&8Cost: &f" + NumberUtils.toReadableNumber(item.getCost()));

                }
                return new MenuElement(builder.build());
            }
        }, 0);

        MenuManager.instance.invalidateInvsForMenu(this);
    }

    public void setupSell() {
        this.setSize(6);
        this.setAll(null);

        MenuElement back = new MenuElement(new ItemBuilder(Material.BED).setName("&6Back").build()).setClickHandler((e, i) -> setupSelect());
        this.setElement(4, back);



        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
