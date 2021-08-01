package com.soraxus.prisons.core.command;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CmdCorePlugin extends SpigotCommand {
    private String prefix;

    public CmdCorePlugin(String alias, String prefix) {
        this.addAlias(alias);
        this.prefix = prefix;
    }
}
