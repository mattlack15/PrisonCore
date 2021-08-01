package com.soraxus.prisons.selling.mutlipliers.command;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CommandMultiplier extends SpigotCommand {
    public CommandMultiplier() {
        addAlias("multiplier");
        addAlias("multi");
        this.addChildren(new CommandMultiplierGive());
    }
}
