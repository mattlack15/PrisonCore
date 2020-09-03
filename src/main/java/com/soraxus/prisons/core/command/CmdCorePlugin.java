package com.soraxus.prisons.core.command;

import net.ultragrav.command.UltraCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CmdCorePlugin extends UltraCommand {
    private String prefix;

    public CmdCorePlugin(String alias, String prefix) {
        this.addAlias(alias);
        this.prefix = prefix;
    }
}
