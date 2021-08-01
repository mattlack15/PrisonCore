package com.soraxus.prisons.debug.commands;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class DebugCmd extends SpigotCommand {
    public DebugCmd() {
        addAlias("debug");

        addChildren(
                new DebugNbtCmd(),
                new DebugChatBuilder(),
                new DebugNpcTestCmd(),
                new DebugWorldId()
        );
    }
}
