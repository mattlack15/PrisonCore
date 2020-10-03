package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.minions.ItemType;
import com.soraxus.prisons.cells.minions.MinionItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class CmdCellMinion extends CellCommand {
    public CmdCellMinion() {
        this.addAlias("minion");
        this.setRequiresCell(true);
    }

    @Override
    protected void perform() {
        getPlayer().getInventory()
                .addItem(MinionItems.getMinionItem("§cMinion", new ItemType(Material.STONE), 1));
    }
}
