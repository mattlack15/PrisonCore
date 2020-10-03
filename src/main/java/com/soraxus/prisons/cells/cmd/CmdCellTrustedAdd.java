package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.cells.TrustedPlayer;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class CmdCellTrustedAdd extends CellCommand {
    public CmdCellTrustedAdd() {
        this.addAlias("add");
        this.addParameter(StringProvider.getInstance(), "player");
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            Cell cell = getOrLoadCell();
            String player = getArgument(0);
            List<Player> matches = Bukkit.matchPlayer(player);
            if(matches.size() == 0) {
                tell(ModuleCells.PREFIX + "&cCould not find that player");
                return;
            }

            Player p = matches.get(0);
            TrustedPlayer trustedPlayer = new TrustedPlayer(p.getName(), p.getUniqueId());

            if(cell.getTrustedPlayers().contains(trustedPlayer)) {
                tell(ModuleCells.PREFIX + "You have already trusted that player...?");
                return;
            }

            cell.addTrustedPlayer(trustedPlayer);
            tell(ModuleCells.PREFIX + "You have trusted &a" + p.getName());
        });
    }
}
