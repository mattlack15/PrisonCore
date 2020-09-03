package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.core.command.OfflinePlayerProvider;
import com.soraxus.prisons.gangs.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangPromote extends GangCommand {
    public CmdGangPromote() {
        this.addAlias("promote");

        this.setRequiresGang(true);

        this.addParameter(OfflinePlayerProvider.getInstance());
    }

    @Override
    public void perform() {
        getAsyncExecutor().submit(() -> {
            OfflinePlayer op = getArgument(0);
            GangMember member = GangMemberManager.instance.getMember(op);
            if (member == null || member.getGang() == null) {
                tell(PREFIX + "&cThat player is not in a gang!");
                return;
            }
            Gang gang = GangManager.instance.getLoadedGang(member.getGang());
            if (gang == null) {
                tell(PREFIX + ChatColor.RED + "Player is not in a gang!");
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
            if (member.getGangRole().ordinal() == GangRole.values().length - 2) {
                tell(PREFIX + "&eThat player is already at the highest role! Maybe you meant to &6demote&e them?");
                return;
            }
            member.setGangRole(GangRole.values()[member.getGangRole().ordinal() + 1]);
            gang.broadcastMessage("&e" + op.getName() + " was promoted to " + member.getGangRole().name().toLowerCase() + "!");
        });
    }
}
