package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.minions.ItemType;
import com.soraxus.prisons.cells.minions.MinionItems;
import com.soraxus.prisons.worldedit.cmd.MaterialDataProvider;
import org.bukkit.Material;

public class CmdCellMinion extends CellCommand {
    public CmdCellMinion() {
        this.addAlias("minion");
        this.setRequiresCell(true);
        this.addParameter(MaterialDataProvider.getInstance(), "type");
    }

    @Override
    protected void perform() {
        int type = getArgument(0);

        getPlayer().getInventory()
                .addItem(MinionItems.getMinionItem("§cMinion", new ItemType(Material.getMaterial(type & 0xFFF)), 1));
    }
}
