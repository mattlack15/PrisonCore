package com.soraxus.prisons.worldedit;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.worldedit.cmd.CmdAsyncWorld;

public class ModuleWorldEdit extends CoreModule {

    public static ModuleWorldEdit instance;

    @Override
    public String getName() {
        return "worldedit";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        instance = this;
        new WorldEditPlayerManager(this);
        new CmdAsyncWorld().register();
    }

    @Override
    protected void onDisable() {

    }
}
