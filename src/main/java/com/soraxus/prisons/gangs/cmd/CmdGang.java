package com.soraxus.prisons.gangs.cmd;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import org.bukkit.ChatColor;

public class CmdGang extends SpigotCommand {
    public static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&d&lGangs &8&l▶ &f");

    public CmdGang() {
        this.addAlias("gang");

        this.setHelpHeader(PREFIX + "&8&m-------&d&l Gangs &8&m-------");
        this.setHelpFooter(null);
        this.setHelpFormat(PREFIX + "&e/&f<cmd> &7<args>");

        this.addChildren(
                new CmdGangCreate(),
                new CmdGangInfo(),
                new CmdGangJoin(),
                new CmdGangLeave(),
                new CmdGangInvite(),
                new CmdGangUnInvite(),
                new CmdGangKick(),
                new CmdGangPromote(),
                new CmdGangDemote(),
                new CmdGangDisband(),
                new CmdGangLeader(),
                new CmdGangRename(),
                new CmdGangDesc(),
                new CmdGangBunker(),
                new CmdGangMine(),
                new CmdGangList(),
                new CmdGangChat(),
                new CmdGangAdmin()
        );
    }
}
