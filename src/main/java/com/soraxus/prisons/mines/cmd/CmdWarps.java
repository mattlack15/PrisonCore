package com.soraxus.prisons.mines.cmd;

import com.soraxus.prisons.mines.gui.MenuWarps;
import net.ultragrav.command.UltraCommand;

public class CmdWarps extends UltraCommand {
    public CmdWarps() {
        this.addAlias("warps");
        this.setRequirePermission(false);
    }

    @Override
    protected void perform() {
        //Open GUI
        new MenuWarps(getPlayer().getUniqueId()).open(getPlayer());
    }
}
