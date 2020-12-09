package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
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
            if(getGangMember().getGang() != null && getGangMember().getGang().equals(gang.getId())) {
                new ChatBuilder(PREFIX + "You are already in this gang...?").send(getPlayer());
                return;
            }
            if (!gang.addMemberWithCondition(getGangMember(), () -> {
                if (gang.isInvited(getGangMember().getMember())) {
                    gang.unInvite(getGangMember().getMember());
                    return true;
                } else return sender.hCasPermission("gang.admin");
            })) {
                tell(PREFIX + ChatColor.RED + "You are not invited to this gang!");
                return;
            }
            gang.broadcastMessage("&a" + sender.getName() + "&f joined the gang!");
        });
    }
}
