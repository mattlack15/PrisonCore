package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangUnInvite extends GangCommand {
    public CmdGangUnInvite() {
        this.addAlias("uninvite");

        this.setRequiresGang(true);

        this.addParameter(StringProvider.getInstance());
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            GangMember member = getGangMember();
            Gang gang = getGang();
            if (!member.getGangRole().isCanInvite()) {
                tell(PREFIX + ChatColor.RED + "Your current role cannot un-invite players to your gang!");
                return;
            }
            String pl = getArgument(0);
            pl = gang.checkInvited(pl);
            if (pl == null) {
                tell(PREFIX + ChatColor.RED + "Player has not been invited!");
                return;
            }
            gang.unInvite(pl);
            gang.broadcastMessage("&e" + getSpigotPlayer().getName() + " &7un-invited &9" + pl + " &7to the gang!");

            Player player = Bukkit.getPlayer(pl);
            //Set to BukkitRunnable if causes concurrency issues
            if (player != null) {
                player.sendMessage(PREFIX + "You were un-invited from " + gang.getName());
            }
        });
    }
}
