package com.soraxus.prisons.shop.customshop;

import com.soraxus.prisons.shop.ModuleShop;
import com.soraxus.prisons.shop.customshop.menu.MenuCustomShop;
import net.ultragrav.command.UltraCommand;

public class CmdGlobalShop extends UltraCommand {

    public CmdGlobalShop() {
        this.addAlias("globalshop");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        new MenuCustomShop(ModuleShop.getGlobalShop(), true).open(getPlayer());
    }
}
