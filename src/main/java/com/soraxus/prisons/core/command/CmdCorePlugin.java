package com.soraxus.prisons.core.command;

import net.ultragrav.command.UltraCommand;

public class CmdCorePlugin extends UltraCommand {
    private String prefix;

    public CmdCorePlugin(String alias, String prefix) {
        this.addAlias(alias);
        this.prefix = prefix;
    }
}
