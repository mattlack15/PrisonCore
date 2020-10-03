package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.CellManager;
import com.soraxus.prisons.cells.CellSettings;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.util.Synchronizer;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class CmdCellVisit extends CellCommand {
    public CmdCellVisit() {
        this.addAlias("visit");
        this.addParameter(StringProvider.getInstance(), "player");
    }

    @Override
    protected void perform() {
        String s = getArgument(0);
        getAsyncExecutor().submit(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(s);
            if(player == null) {
                tell(ModuleCells.PREFIX + "&cCould not find that player");
                return;
            }

            Cell cell = CellManager.instance.deserializeCell(player.getUniqueId());
            CellSettings.OpenSetting setting = cell.getSettings().getOpenSetting();
            if(!getPlayer().getUniqueId().equals(player.getUniqueId())) {
                if (setting == CellSettings.OpenSetting.CLOSED) {
                    tell(ModuleCells.PREFIX + "&cHmmm... the bouncer told me that the owner has not made this cell visitable.");
                    return;
                } else if (setting == CellSettings.OpenSetting.TRUSTED && !cell.isTrusted(getPlayer().getUniqueId())){
                    tell(ModuleCells.PREFIX + "&cHmmm... the bouncer told me that the owner has only made this cell visitable to trusted players.");
                    return;
                }
            }

            cell = CellManager.instance.loadCell(player.getUniqueId()).join();
            if(cell == null) {
                tell(ModuleCells.PREFIX + "&cThat player doesn't seem to have a cell.");
                return;
            }

            tell(ModuleCells.PREFIX + "Sending you to &a" + player.getName() + ChatColor.getLastColors(ModuleCells.PREFIX) + "'s cell");

            Location loc = cell.getBoundingRegion().getCenter().toBukkitVector().setY(61.1D).toLocation(cell.getWorld().getBukkitWorld());
            Synchronizer.synchronize(() -> getPlayer().teleport(loc));
        });
    }
}
