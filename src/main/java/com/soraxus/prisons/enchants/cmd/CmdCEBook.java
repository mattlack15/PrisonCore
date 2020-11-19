package com.soraxus.prisons.enchants.cmd;

import net.ultragrav.command.UltraCommand;

public class CmdCEBook extends UltraCommand {
    public CmdCEBook() {
        this.addAlias("book");
        addChildren(new CmdCEBookGive());
    }
}
