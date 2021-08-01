package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.GangRole;
import net.ultragrav.command.provider.impl.StringProvider;

public class CmdGangDesc extends GangCommand {
    public CmdGangDesc() {
        this.addAlias("desc");

        this.setRequiresGang(true);

        this.addParameter(StringProvider.getInstance(), "description");
    }

    public void perform() {
        if (getGangMember().getGangRole().ordinal() >= GangRole.ADMIN.ordinal()) {
            getGang().setDescription(getArgument(0));
            getGang().broadcastMessage("The gang description was set to &e" + getGang().getDescription() + " &fby &a" + getSpigotPlayer().getName());
        } else
            tell(CmdGang.PREFIX + "&cYou are not a high enough role to do this!");
    }
}
