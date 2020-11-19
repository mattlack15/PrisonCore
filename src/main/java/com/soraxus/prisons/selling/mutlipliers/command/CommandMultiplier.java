package com.soraxus.prisons.selling.mutlipliers.command;

import net.ultragrav.command.UltraCommand;

public class CommandMultiplier extends UltraCommand {
    public CommandMultiplier() {
        addAlias("multiplier");
        addAlias("multi");
        this.addChildren(new CommandMultiplierGive());
    }
}
