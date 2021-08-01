package com.soraxus.prisons.debug.commands;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import org.bukkit.Bukkit;

public class DebugWorldId extends SpigotCommand {
    public DebugWorldId() {
        this.addAlias("worldid");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
    }
}
