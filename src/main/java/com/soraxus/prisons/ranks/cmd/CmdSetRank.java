package com.soraxus.prisons.ranks.cmd;

import com.soraxus.prisons.ranks.PRankPlayer;
import com.soraxus.prisons.ranks.RankupManager;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.StringProvider;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.entity.Player;

public class CmdSetRank extends SpigotCommand {
    public CmdSetRank() {
        this.addAlias("setprisonrank");
        this.addParameter(PlayerProvider.getInstance(), "player");
        this.addParameter(StringProvider.getInstance(), "rank");
    }

    @Override
    protected void perform() {
        Player player = getArgument(0);
        String rank = getArgument(1);
        PRankPlayer pRankPlayer = RankupManager.instance.getPlayer(player.getUniqueId());
        int index = RankupManager.instance.getIndex(RankupManager.instance.getRank(rank));
        if(index == -1) {
            tell("&cCould not find that rank!");
            return;
        }
        RankupManager.instance.setRank(getSpigotPlayer(), index);
        tell("&aDone! &7They are now " + RankupManager.instance.getRank(rank).getDisplayName());
    }
}
