package com.soraxus.prisons.worldedit.cmd;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CmdAsyncWorld extends SpigotCommand {
    public CmdAsyncWorld() {
        addAlias("asyncworld");

        addChildren(
                new CmdCopy(),
                new CmdLoadSchematic(),
                new CmdPaste(),
                new CmdWand(),
                new CmdSaveSchematic(),
                new CmdRotate(),
                new CmdSet(),
                new CmdReplace()
        );
    }
}
