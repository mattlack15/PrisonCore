package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.ModuleCells;
import net.ultragrav.command.UltraCommand;

public class CmdCell extends UltraCommand {
    public static final String PREFIX = ModuleCells.PREFIX;

    public CmdCell() {
        this.addAlias("cell");
        this.addAlias("cells");
        this.addAlias("plot");
        this.addAlias("plots");

        this.setAllowConsole(false);
        this.setHelpHeader(PREFIX + "&7&m-------&8&l Cells &7&m-------");
        this.setHelpFooter(null);
        this.setHelpFormat(PREFIX + "&5/&f<cmd> &7<args>");

        this.addChildren(
                new CmdCellCreate(),
                new CmdCellTp(),
                new CmdCellTrusted(),
                new CmdCellSetTime(),
                new CmdCellVisit(),
                new CmdCellPanel(),
                new CmdCellMinion(),
                new CmdCellBypass()
        );
    }
}
