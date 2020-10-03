package com.soraxus.prisons.cells.cmd;

public class CmdCellTrusted extends CellCommand {
    public CmdCellTrusted() {
        this.addAlias("trusted");
        this.setRequiresCell(true);
        this.addChildren(new CmdCellTrustedList(),
                new CmdCellTrustedRemove(),
                new CmdCellTrustedAdd());
    }
}
