package com.soraxus.prisons.core;

import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;

import java.util.ArrayList;
import java.util.List;

public class CoreGUI extends Menu {
    private List<CoreModule> modules;

    public CoreGUI(String title, List<CoreModule> modules) {
        super(title, 5);
        this.modules = modules;
        this.setup();
    }

    public void setup() {

        List<MenuElement> elements = new ArrayList<>();
        modules.forEach(m -> {
            MenuElement element = m.getGUI(getBackButton(this));
            if (element != null)
                elements.add(element);
        });

        this.setupActionableList(10, 9 * 4 - 2, 9 * 4, 9 * 5 - 1, (index) -> {
            if (index >= elements.size())
                return null;
            return elements.get(index);
        }, 0);

    }
}
