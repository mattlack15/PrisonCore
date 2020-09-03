package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.core.command.OfflinePlayerProvider;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangRole;
import org.bukkit.OfflinePlayer;
import sun.misc.Unsafe;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangLeader extends GangCommand {
    public CmdGangLeader() {
        this.addAlias("leader");

        this.setRequiresGang(true);

        this.addParameter(OfflinePlayerProvider.getInstance());
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            GangMember member = getGangMember();
            Gang gang = getGang();
            if (!member.getGangRole().equals(GangRole.LEADER) && !sender.hasPermission("gang.admin")) {
                tell(PREFIX + "&cYou are not the leader of your faction!");
                return;
            }
            boolean forced = !member.getGangRole().equals(GangRole.LEADER);
            OfflinePlayer op = getArgument(0);
            GangMember toLeader = null;
            for (GangMember members : gang.getMembers())
                if (member.getMemberName().equalsIgnoreCase(op.getName()))
                    toLeader = members;
            if (toLeader == null) {
                tell(PREFIX + "&cPlayer is not in your gang!");
                return;
            }
            toLeader.setGangRole(GangRole.LEADER);
            if(forced) {
                gang.broadcastMessage("&a" + toLeader.getMemberName() + "&f was forcibly given leadership of the gang!");
            } else {
                gang.broadcastMessage("&e" + sender.getName() + "&f transferred leadership of the gang to &a" + toLeader.getMemberName() + "&f!");
            }
        });
    }
}
