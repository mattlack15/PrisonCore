package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.CellManager;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.errors.ModuleErrors;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.exception.CommandException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class CellCommand extends UltraCommand {
    @Getter
    @Setter
    private boolean requiresCell = false;

    @Getter
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(2);

    public Cell getOrLoadCell() {
        if(!isPlayer())
            return null;
        Cell cell = CellManager.instance.getLoadedCell(getPlayer().getUniqueId());
        if(cell == null) {
            try {
                cell = CellManager.instance.loadCell(getPlayer().getUniqueId()).join();
            } catch(Throwable t) {
                t.printStackTrace();
                tell(ModuleCells.PREFIX + "&cWe had trouble loading your cell!");
                String errorId = ModuleErrors.instance.recordError(t);
                tell(ModuleCells.PREFIX + "&cPlease send this error code to a developer: &f" + errorId);
                throw t;
            }
        }
        return cell;
    }

    @Override
    protected void preConditions() {
        if(requiresCell) {
            if(!CellManager.instance.cellExists(getPlayer().getUniqueId())) {
                throw new CommandException(ModuleCells.PREFIX + "You must have a cell to use this command. Create one first!");
            }
        }
    }

    @Override
    public boolean isAllowConsole() {
        return this.allowConsole && !this.requiresCell;
    }
}
