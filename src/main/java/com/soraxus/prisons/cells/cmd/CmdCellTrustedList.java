package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.cells.TrustedPlayer;
import org.bukkit.ChatColor;

import java.util.List;

public class CmdCellTrustedList extends CellCommand {
    public CmdCellTrustedList() {
        this.addAlias("list");
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            Cell cell = getOrLoadCell();
            List<TrustedPlayer> trustedPlayerList = cell.getTrustedPlayers();
            tell(ModuleCells.PREFIX + "You have trusted &a" + trustedPlayerList.size() + ChatColor.getLastColors(ModuleCells.PREFIX) + " people!");
            trustedPlayerList.forEach(p -> tell(ModuleCells.PREFIX + " - &7" + p.getName()));
        });
    }
}
