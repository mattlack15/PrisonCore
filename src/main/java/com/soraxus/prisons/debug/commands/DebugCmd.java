package com.soraxus.prisons.debug.commands;

import net.ultragrav.command.UltraCommand;

public class DebugCmd extends UltraCommand {
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
