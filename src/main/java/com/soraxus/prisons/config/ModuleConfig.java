package com.soraxus.prisons.config;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.menus.MenuElement;

public class ModuleConfig extends CoreModule {
    public static ModuleConfig instance;

    @Override
    protected void onEnable() {
        new ConfigManager(this);
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    public String getName() {
        return "Bunkers";
    }
}
