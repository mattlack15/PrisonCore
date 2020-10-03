package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import org.bukkit.ChatColor;

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
            if (!sender.hasPermission("gang.admin") && !gang.addMemberWithCondition(getGangMember(), () -> {
                if(gang.isInvited(getGangMember().getMember())) {
                    gang.unInvite(getGangMember().getMember());
                    return true;
                }
                return false;
            })) {
                tell(PREFIX + ChatColor.RED + "You are not invited to this gang!");
                return;
            }
            gang.broadcastMessage("&a" + sender.getName() + "&f joined the gang!");
        });
    }
}
