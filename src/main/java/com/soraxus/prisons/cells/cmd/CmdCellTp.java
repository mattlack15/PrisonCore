package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.util.Synchronizer;
import org.bukkit.Location;

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
            Location loc = cell.getBoundingRegion().getCenter().toBukkitVector().setY(61.1D).toLocation(cell.getWorld().getBukkitWorld());
            Synchronizer.synchronize(() -> getPlayer().teleport(loc));
        });
    }
}
