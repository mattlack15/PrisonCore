package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangRole;
import net.ultragrav.command.provider.impl.StringProvider;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangLeader extends GangCommand {
    public CmdGangLeader() {
        this.addAlias("leader");

        this.setRequiresGang(true);

        this.addParameter(StringProvider.getInstance(), "player");
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            GangMember member = getGangMember();
            Gang gang = getGang();
            if (!member.getGangRole().equals(GangRole.LEADER) && !sender.hasPermission("gang.admin")) {
                tell(PREFIX + "&cYou are not the leader of your gang!");
                return;
            }
            boolean forced = !member.getGangRole().equals(GangRole.LEADER);
            String op = getArgument(0);
            GangMember toLeader = null;
            for (GangMember members : gang.getMembers()) {
                if (members.getMemberName().equalsIgnoreCase(op)) {
                    toLeader = members;
                    break;
                }
            }
            if (toLeader == null) {
                tell(PREFIX + "&cPlayer is not in your gang!");
                return;
            }

            if (getGangMember().getGangRole() == GangRole.LEADER)
                getGangMember().setGangRole(GangRole.ADMIN);

            toLeader.setGangRole(GangRole.LEADER);

            if (forced) {
                gang.broadcastMessage("&a" + toLeader.getMemberName() + "&f was forcibly given leadership of the gang!");
            } else {
                gang.broadcastMessage("&e" + getSpigotPlayer().getName() + "&f transferred leadership of the gang to &a" + toLeader.getMemberName() + "&f!");
            }
        });
    }
}
