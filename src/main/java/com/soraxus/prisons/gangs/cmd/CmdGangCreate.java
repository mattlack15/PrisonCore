package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.*;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangCreate extends GangCommand {
    public CmdGangCreate() {
        this.addAlias("create");

        this.setAllowConsole(false);

        this.addParameter(StringProvider.getInstance(), "name");
    }

    public void perform() {
        if (getGang() != null) {
            returnTell(PREFIX + ChatColor.RED + "You are already in a gang!");
        }
        Player player = getPlayer();

        String name = getArgument(0);

        if (GangManager.instance.getLoadedGang(name) != null)
            returnTell(PREFIX + "&cGang already exists! Maybe try fighting them for it?");

        char[] chs = name.toCharArray();
        if (chs.length > 26) {
            returnTell(PREFIX + "&cGang name is too long!");
        }
        for (char ch : chs) {
            if (!Character.isAlphabetic(ch) && !Character.isDigit(ch)) {
                returnTell(PREFIX + "&cGang name must be alphanumeric!");
            }
        }

        Gang gang = GangManager.instance.createGang(name);
        GangMember member = GangMemberManager.instance.getOrMakeMember(player);
        gang.addMember(member);
        member.setGangRole(GangRole.LEADER);
        Bukkit.broadcastMessage(PREFIX + ChatColor.GRAY + player.getName() + " created a gang named " + ChatColor.YELLOW + name + ChatColor.GRAY + "!");
    }
}
