package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangRole;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.ChatColor;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangRename extends GangCommand {

    public CmdGangRename() {
        this.addAlias("rename");
        this.setRequiresGang(true);
        this.addParameter(StringProvider.getInstance(), "new name");
    }

    public void perform() {
        GangMember member = getGangMember();
        if (!member.getGangRole().equals(GangRole.LEADER))
            returnTell(PREFIX + ChatColor.RED + "You are not the leader of your gang!");
        Gang gang = getGang();
        String newName = getArgument(0);
        if (GangManager.instance.gangExists(newName))
            returnTell(PREFIX + ChatColor.RED + "Gang already exists! Maybe try fighting them for it?");
        gang.setName(newName);
        gang.broadcastMessage(PREFIX + "Your gang was renamed to " + ChatColor.GREEN + gang.getName());
    }
}
