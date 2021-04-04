package com.soraxus.prisons.cells.cmd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setUncaughtExceptionHandler((a, b) -> b.printStackTrace()).build());

    public Cell getOrLoadCell() {
        if (!isPlayer())
            return null;
        Cell cell = CellManager.instance.getLoadedCell(getPlayer().getUniqueId());
        if (cell == null) {
            try {
                cell = CellManager.instance.loadCell(getPlayer().getUniqueId()).join();
            } catch (Throwable t) {
                t.printStackTrace();
                ModuleErrors.instance.getErrorMessage(t, "loading cell", "uhhhm... done nothing").send(getPlayer());
                throw t;
            }
        }
        return cell;
    }

    @Override
    protected void preConditions() {
        if (requiresCell) {
            if (!CellManager.instance.cellExists(getPlayer().getUniqueId())) {
                throw new CommandException(ModuleCells.PREFIX + "You must have a cell to use this command. Create one first!");
            }
        }
    }

    @Override
    public boolean isAllowConsole() {
        return this.allowConsole && !this.requiresCell;
    }
}
