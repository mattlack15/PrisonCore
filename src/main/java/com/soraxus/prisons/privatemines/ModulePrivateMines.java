package com.soraxus.prisons.privatemines;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.menus.MenuElement;

import java.io.File;

public class ModulePrivateMines extends CoreModule {

    public static ModulePrivateMines instance;

    @Override
    protected void onEnable() {
        instance = this;
        new PrivateMineManager(new File(getDataFolder(), "mines"));
    }

    @Override
    protected void onDisable() {
        PrivateMineManager.instance.getLoadedPrivateMines().forEach(p -> PrivateMineManager.instance.saveAndUnloadPrivateMineSync(p));
        PrivateMineManager.instance.saveCache();
    }

    @Override
    public String getName() {
        return "Private Mines";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

}
