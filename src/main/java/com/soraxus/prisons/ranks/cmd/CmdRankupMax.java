package com.soraxus.prisons.ranks.cmd;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.ranks.PRankPlayer;
import com.soraxus.prisons.ranks.Rank;
import com.soraxus.prisons.ranks.RankupManager;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import org.bukkit.Bukkit;

public class CmdRankupMax extends SpigotCommand {
    public CmdRankupMax() {
        this.addAlias("rankupmax");
        this.addAlias("rumax");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        PRankPlayer rankPlayer = RankupManager.instance.getPlayer(getPlayer().getUniqueId());
        while(true) {
            Rank rank = RankupManager.instance.getRank(rankPlayer.getRankIndex() + 1);
            if (rank == null) {
                tell("&aDone! You've reached the tippity top!");
                return;
            }
            long cost = rank.getCostForPrestige(rankPlayer.getPrestige());
            if (!Economy.money.tryRemoveBalance(getPlayer().getUniqueId(), cost)) {
                tell("&aDone! You're pretty much out of money now :)");
                return;
            }
            rankPlayer.setRankIndex(rankPlayer.getRankIndex() + 1);
            rank.getCmds().forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", getSpigotPlayer().getName())));
            tell("&aYou have ranked up to " + rank.getDisplayName() + "!");
        }
    }
}
