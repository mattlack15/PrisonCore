package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.cells.TrustedPlayer;
import net.ultragrav.command.provider.impl.StringProvider;

public class CmdCellTrustedRemove extends CellCommand {
    public CmdCellTrustedRemove() {
        this.addAlias("remove");
        this.addParameter(StringProvider.getInstance(), "player");
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            Cell cell = getOrLoadCell();
            String player = getArgument(0);

            TrustedPlayer trustedPlayer = null;

            for (TrustedPlayer cellTrustedPlayer : cell.getTrustedPlayers()) {
                if (cellTrustedPlayer.getName().equalsIgnoreCase(player)) {
                    trustedPlayer = cellTrustedPlayer;
                    break;
                }
            }

            if (trustedPlayer == null) {
                tell(ModuleCells.PREFIX + "&cYou have not trusted that player");
                return;
            }

            cell.removeTrustedPlayer(trustedPlayer);
            tell(ModuleCells.PREFIX + "You no longer trust &c" + trustedPlayer.getName());
        });
    }
}
