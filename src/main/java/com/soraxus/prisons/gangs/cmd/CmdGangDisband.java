package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangRole;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangDisband extends GangCommand {
    public CmdGangDisband() {
        this.addAlias("disband");

        this.setRequiresGang(true);
    }

    public void perform() {
        getAsyncExecutor().execute(() -> {
            GangMember member = getGangMember();
            Gang gang = getGang();
            if (!member.getGangRole().equals(GangRole.LEADER)) {
                tell(PREFIX + "&cYou must be the leader to disband your gang!");
                return;
            }
            gang.broadcastMessage(PREFIX + "Your gang was disbanded by &e" + getPlayer().getName() + "!");
            gang.disband();
            tell(PREFIX + "Gang disbanded!");
        });
    }
}
