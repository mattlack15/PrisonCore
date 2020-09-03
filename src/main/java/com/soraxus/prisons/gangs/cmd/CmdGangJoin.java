package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.core.command.GravSubCommand;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangJoin extends GangCommand {
    public CmdGangJoin() {
        this.addAlias("join");

        this.setAllowConsole(false);

        this.addParameter(GangProvider.getInstance(), "gang");
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            Gang gang = getArgument(0);
            if (!gang.isInvited(getPlayer().getUniqueId()) && !sender.hasPermission("gang.admin")) {
                tell(PREFIX + ChatColor.RED + "You are not invited to this gang!");
                return;
            }
            gang.unInvite(((Player) sender).getUniqueId());
            gang.addMember(GangMemberManager.instance.getMember(((Player) sender).getUniqueId()));
            gang.broadcastMessage("&a" + sender.getName() + "&f joined the gang!");
        });
    }
}
