package com.soraxus.prisons.enchants.cmd;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CmdCEBook extends SpigotCommand {
    public CmdCEBook() {
        this.addAlias("book");
        addChildren(new CmdCEBookGive());
    }
}
