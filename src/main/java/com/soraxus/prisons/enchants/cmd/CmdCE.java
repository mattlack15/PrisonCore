package com.soraxus.prisons.enchants.cmd;

import net.ultragrav.command.UltraCommand;

public class CmdCE extends UltraCommand {
    public CmdCE() {
        addAlias("ce");
        addAlias("customenchants");
        addChildren(new CmdCEBook());
    }
}
