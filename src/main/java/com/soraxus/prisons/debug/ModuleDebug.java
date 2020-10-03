package com.soraxus.prisons.debug;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.debug.commands.DebugCmd;
import com.soraxus.prisons.util.menus.MenuElement;

public class ModuleDebug extends CoreModule {
    @Override
    public String getName() {
        return "Debug";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        new DebugCmd().register();
    }
}
