package com.soraxus.prisons.ranks.cmd;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.ranks.PRankPlayer;
import com.soraxus.prisons.ranks.Rank;
import com.soraxus.prisons.ranks.RankupManager;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import org.bukkit.Bukkit;

public class CmdPrestige extends SpigotCommand {
    public CmdPrestige() {
        this.addAlias("prestige");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        PRankPlayer rankPlayer = RankupManager.instance.getPlayer(getPlayer().getUniqueId());
        Rank rank = RankupManager.instance.getRank(rankPlayer.getRankIndex() + 1);
        if(rank != null) {
            tell("&cYou are not the top rank!");
            return;
        }

        rankPlayer.setRankIndex(0);
        rankPlayer.setPrestige(rankPlayer.getPrestige() + 1);
        Economy.money.setBalance(getPlayer().getUniqueId(), 0L);

        RankupManager.instance.getPrestigeCmds().forEach(p -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), p.replace("%player%", getSpigotPlayer().getName())));

        tell("");
        tell("&aYou prestriged :DDD");
        tell("&7Prestiged* oops");
        tell("");
    }
}
