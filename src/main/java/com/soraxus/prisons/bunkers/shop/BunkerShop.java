package com.soraxus.prisons.bunkers.shop;

import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class BunkerShop {
    @Getter
    private final String name;
    @Getter
    private final List<BunkerShopSection> sectionList = new ArrayList<>();

    public BunkerShop(String name) {
        this.name = name;
    }

    public synchronized List<BunkerShopSection> getSections() {
        return new ArrayList<>(sectionList);
    }

    public synchronized void addSection(BunkerShopSection section) {
        sectionList.add(section);
    }

    public synchronized Menu getMenu(MenuElement backButton) {
        return new BunkerShopMenu(this, backButton);
    }
}
