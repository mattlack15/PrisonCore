package com.soraxus.prisons.ranks;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.menus.MenuElement;

public class ModuleRanks extends CoreModule {
    @Override
    public String getName() {
        return "Rank";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        new RankupManager(this);
    }
}
