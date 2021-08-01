package com.soraxus.prisons.enchants.cmd;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CmdCE extends SpigotCommand {
    public CmdCE() {
        addAlias("ce");
        addAlias("customenchants");
        addChildren(new CmdCEBook());
    }
}
