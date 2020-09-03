package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMemberManager;
import com.soraxus.prisons.gangs.GangRole;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangInfo extends GangCommand {
    public CmdGangInfo() {
        this.addAlias("info");

        this.addParameter(null, StringProvider.getInstance(), "gang");
    }

    public void perform() {
        String gangName = getArgument(0);
        UUID gangId;
        if (gangName == null) {
            if (isPlayer()) {
                gangId = GangMemberManager.instance.getMember(getPlayer()).getGang();
                if (gangId == null) {
                    returnTell(PREFIX + "§cYou are not in a gang, try /gang info <Gang>");
                }
            } else {
                returnTell(PREFIX + "&cMissing argument! (gang)");
                return;
            }
        } else {
            gangId = GangManager.instance.getId(gangName);
        }
        getAsyncExecutor().execute(() -> {
            try {
                Gang gang = GangManager.instance.loadGang(gangId);
                if (gang == null) {
                    tell(PREFIX + ChatColor.RED + "Gang does not exist!");
                    return;
                }

                tell(PREFIX + ChatColor.BLUE + "Name > &f" + gang.getName());
                tell(PREFIX + ChatColor.BLUE + "Description > &f" + gang.getDescription());
                tell(PREFIX + ChatColor.BLUE + "Members > &e" + gang.getMembers().stream()
                        .map((gm) -> {
                            if (gm.getGangRole().equals(GangRole.LEADER)) {
                                return "**" + gm.getMemberName();
                            } else if (gm.getGangRole().equals(GangRole.ADMIN)) {
                                return "*" + gm.getMemberName();
                            }
                            return gm.getMemberName();
                        })
                        .collect(Collectors.joining(", "))
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
