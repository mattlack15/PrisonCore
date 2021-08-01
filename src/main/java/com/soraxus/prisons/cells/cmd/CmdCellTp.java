package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.util.Synchronizer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class CmdCellTp extends CellCommand {
    public CmdCellTp() {
        this.addAlias("tp");
        this.addAlias("teleport");
        this.setRequiresCell(true);
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            Cell cell = getOrLoadCell();
            Location loc = new Vector(33.5, 61.1D, 28.5).toLocation(cell.getWorld().getBukkitWorld());
            Synchronizer.synchronize(() -> getSpigotPlayer().teleport(loc));
        });
    }
}
