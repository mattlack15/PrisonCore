package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.ModuleCells;

public class CmdCellBypass extends CellCommand {
    public CmdCellBypass() {
        this.addAlias("bypass");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        ModuleCells.instance.setBypassMode(getPlayer().getUniqueId(), !ModuleCells.instance.getBypassMode(getPlayer().getUniqueId()));
        tell(CmdCell.PREFIX + "Bypass mode " + (ModuleCells.instance.getBypassMode(getPlayer().getUniqueId()) ? "&aenabled" : "&cdisabled"));
    }
}
