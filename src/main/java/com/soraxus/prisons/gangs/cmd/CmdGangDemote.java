package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.core.command.OfflinePlayerProvider;
import com.soraxus.prisons.gangs.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangDemote extends GangCommand {
    public CmdGangDemote() {
        this.addAlias("demote");

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
            if (sender.hasPermission("gang.admin")) {
                if (member.getGangRole().ordinal() == 0) {
                    tell(PREFIX + "&eThat player is already at the lowest role! Maybe you meant to &6promote&e them?");
                    return;
                }
            } else {
                UUID senderId = ((Player) sender).getUniqueId();
                GangMember senderMember = GangMemberManager.instance.getMember(senderId);
                Gang senderGang = GangManager.instance.getLoadedGang(senderMember.getGang());
                if (senderGang == null || !senderGang.equals(gang) || senderMember.getGangRole().ordinal() < 2 || senderMember.getGangRole().ordinal() <= member.getGangRole().ordinal()) {
                    tell(PREFIX + "&cYou do not have permission to do this!");
                    return;
                }
                if (member.getGangRole().ordinal() == 0) {
                    tell(PREFIX + "&eThat player is already at the lowest role! Maybe you meant to &6promote&e them?");
                    return;
                }
            }
            member.setGangRole(GangRole.values()[member.getGangRole().ordinal() - 1]);
            gang.broadcastMessage("&e" + op.getName() + " was demoted to " + member.getGangRole().name().toLowerCase() + "!");
        });
    }
}
