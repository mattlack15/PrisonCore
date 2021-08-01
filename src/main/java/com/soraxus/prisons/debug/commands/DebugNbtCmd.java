package com.soraxus.prisons.debug.commands;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class DebugNbtCmd extends SpigotCommand {
    public DebugNbtCmd() {
        addAlias("nbt");

        addChildren(
                new DebugNbtListCmd()
        );
    }
}
