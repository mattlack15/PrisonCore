package com.soraxus.prisons.shop.customshop;

import com.soraxus.prisons.shop.ModuleShop;
import com.soraxus.prisons.shop.customshop.menu.MenuCustomShop;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CmdGlobalShop extends SpigotCommand {

    public CmdGlobalShop() {
        this.addAlias("globalshop");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        new MenuCustomShop(ModuleShop.getGlobalShop(), true).open(getSpigotPlayer());
    }
}
