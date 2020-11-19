package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangLeave extends GangCommand {
    public CmdGangLeave() {
        this.addAlias("leave");

        this.setRequiresGang(true);
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            GangMember member = GangMemberManager.instance.getMember(getPlayer());
            Gang gang = getGang();
            member.setGang(null);
            gang.broadcastMessage("&e" + sender.getName() + "&f left the gang!");
            tell(PREFIX + "You left your gang!");
        });
    }
}
