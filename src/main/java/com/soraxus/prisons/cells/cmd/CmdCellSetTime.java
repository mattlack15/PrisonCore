package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.ModuleCells;
import net.ultragrav.command.provider.impl.IntegerProvider;

public class CmdCellSetTime extends CellCommand {
    public CmdCellSetTime() {
        this.addAlias("settime");
        this.setRequiresCell(true);
        this.addParameter(IntegerProvider.getInstance(), "time");
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            getOrLoadCell().setSettingWorldTime(this.<Integer>getArgument(0));
            tell(ModuleCells.PREFIX + "The time in your cell has been set to &e" + getArgument(0));
        });
    }
}
