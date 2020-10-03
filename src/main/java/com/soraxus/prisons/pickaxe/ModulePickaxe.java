package com.soraxus.prisons.pickaxe;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.pickaxe.crystals.CrystalManager;
import com.soraxus.prisons.pickaxe.crystals.command.CrystalsCmd;
import com.soraxus.prisons.pickaxe.levels.PickaxeLevelManager;
import com.soraxus.prisons.util.menus.MenuElement;

public class ModulePickaxe extends CoreModule {
    @Override
    public String getName() {
        return "Pickaxes";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        new PickaxeLevelManager();
        new CrystalsCmd().register();
    }
}
