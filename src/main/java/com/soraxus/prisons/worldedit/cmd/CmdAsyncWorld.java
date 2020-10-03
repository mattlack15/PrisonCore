package com.soraxus.prisons.worldedit.cmd;

import net.ultragrav.command.UltraCommand;

public class CmdAsyncWorld extends UltraCommand {
    public CmdAsyncWorld() {
        addAlias("asyncworld");

        addChildren(
                new CmdCopy(),
                new CmdLoadSchematic(),
                new CmdPaste(),
                new CmdWand(),
                new CmdSaveSchematic(),
                new CmdRotate()
        );
    }
}