package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.cmd.admin.CmdGangAdminXp;
import net.ultragrav.command.UltraCommand;
import org.bukkit.ChatColor;

public class CmdGangAdmin extends UltraCommand {
    public static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&d&lGangs &8&l▶ &f");

    public CmdGangAdmin() {
        this.addAlias("admin");

        this.addChildren(
                new CmdGangAdminXp()
        );
    }
}
