package com.soraxus.prisons.debug.commands;

import net.ultragrav.command.UltraCommand;

public class DebugNbtCmd extends UltraCommand {
    public DebugNbtCmd() {
        addAlias("nbt");

        addChildren(
                new DebugNbtListCmd()
        );
    }
}
