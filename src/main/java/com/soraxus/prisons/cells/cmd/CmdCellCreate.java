package com.soraxus.prisons.cells.cmd;

import com.soraxus.prisons.cells.CellManager;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.ChatColor;

public class CmdCellCreate extends CellCommand {
    public CmdCellCreate() {
        this.addAlias("create");
        this.setAllowConsole(false);

        this.addParameter("", StringProvider.getInstance(), "confirm");
    }

    @Override
    protected void perform() {
        getAsyncExecutor().submit(() -> {
            try {
                if (CellManager.instance.cellExists(getPlayer().getUniqueId())) {
                    tell(ModuleCells.PREFIX + "&cYou already have a cell!");
                    if (CellManager.instance.getLoadedCell(getPlayer().getUniqueId()) == null) {
                        tell(ModuleCells.PREFIX + "It isn't loaded though so we'll do that for you...");
                        getOrLoadCell();
                        tell(ModuleCells.PREFIX + "Loaded!");
                    }
                    return;
                }

                Economy economy = Economy.money;
                if (!economy.hasBalance(getPlayer().getUniqueId(), ModuleCells.CELL_COST)) {
                    tell(ModuleCells.PREFIX + "&cYou don't have enough &cm&6u&el&2l&ba&5h &cto buy a cell. &7(money)");
                    return;
                }

                String confirm = getArgument(0);
                if (confirm.isEmpty()) {
                    new ChatBuilder(ModuleCells.PREFIX + "It will cost &c$" + ModuleCells.CELL_COST + ChatColor.getLastColors(ModuleCells.PREFIX) + " to buy a cell... ")
                            .addText(
                                    "&a&lConfirm? &7(Click)",
                                    HoverUtil.text("&7Click to confirm"),
                                    ClickUtil.command("/cell create confirm")
                            )
                            .send(getSpigotPlayer());
                    return;
                }

                economy.removeBalance(getPlayer().getUniqueId(), ModuleCells.CELL_COST);

                tell(ModuleCells.PREFIX + "Creating your cell...");
                CellManager.instance.createCell(getPlayer().getUniqueId()).join();
                new ChatBuilder(ModuleCells.PREFIX + "Your cell has been created.")
                        .addText(
                                "&a click here to teleport to it!",
                                HoverUtil.text("&cTeleport"),
                                ClickUtil.command("/cell tp")
                        )
                        .send(getSpigotPlayer());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
