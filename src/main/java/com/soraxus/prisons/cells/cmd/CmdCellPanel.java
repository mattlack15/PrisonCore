package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.panel.MenuCellPanel;
import com.soraxus.prisons.util.Synchronizer;

public class CmdCellPanel extends CellCommand {
    public CmdCellPanel() {
        this.addAlias("panel");
        this.setRequiresCell(true);
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            Cell cell = getOrLoadCell();
            Synchronizer.synchronize(() -> new MenuCellPanel(cell).open(getSpigotPlayer()));
        });
    }
}
