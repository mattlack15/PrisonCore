package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.core.command.OfflinePlayerProvider;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;
import org.bukkit.OfflinePlayer;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangKick extends GangCommand {
    public CmdGangKick() {
        this.addAlias("kick");

        this.setRequiresGang(true);

        this.addParameter(OfflinePlayerProvider.getInstance());
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            OfflinePlayer op = getArgument(0);
            GangMember member = GangMemberManager.instance.getMember(op);
            if (member == null || member.getGang() == null) {
                tell(PREFIX + "&cPlayer is not in a gang!");
                return;
            }
            Gang gang = GangManager.instance.getLoadedGang(member.getGang());
            if (gang == null) {
                tell(PREFIX + "&cPlayer is not in a gang!");
                return;
            }
            if (!sender.hasPermission("gang.admin")) {
                GangMember senderMember = getGangMember();
                Gang senderGang = getGang();
                if (senderGang == null || !senderGang.equals(gang) || senderMember.getGangRole().ordinal() < 2 || senderMember.getGangRole().ordinal() <= member.getGangRole().ordinal()) {
                    tell(PREFIX + "&cYou do not have permission to do this!");
                    return;
                }
            }
            member.setGang(null);
            gang.broadcastMessage("&e" + op.getName() + " was kicked from the gang!");
        });
    }
}
