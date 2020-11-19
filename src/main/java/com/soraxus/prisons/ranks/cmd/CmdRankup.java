package com.soraxus.prisons.ranks.cmd;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.ranks.PRankPlayer;
import com.soraxus.prisons.ranks.Rank;
import com.soraxus.prisons.ranks.RankupManager;
import com.soraxus.prisons.util.NumberUtils;
import net.ultragrav.command.UltraCommand;
import org.bukkit.Bukkit;

public class CmdRankup extends UltraCommand {
    public CmdRankup() {
        this.addAlias("rankup");
        this.addAlias("ru");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        PRankPlayer rankPlayer = RankupManager.instance.getPlayer(getPlayer().getUniqueId());
        Rank rank = RankupManager.instance.getRank(rankPlayer.getRankIndex() + 1);
        if(rank == null) {
            tell("&cThere is no more ranking up to do... You've reached the tippity top!");
            tell("&6You can prestige however, use /prestige");
            return;
        }
        long cost = rank.getCostForPrestige(rankPlayer.getPrestige());
        if(!Economy.money.tryRemoveBalance(getPlayer().getUniqueId(), cost)) {
            tell("&cYou don't have the required &4$" + NumberUtils.toReadableNumber(cost));
            return;
        }
        rankPlayer.setRankIndex(rankPlayer.getRankIndex() + 1);
        rank.getCmds().forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", getPlayer().getName())));
        tell("&aYou have ranked up to " + rank.getDisplayName() + "!");
    }
}
