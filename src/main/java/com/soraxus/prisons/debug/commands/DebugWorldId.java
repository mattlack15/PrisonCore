package com.soraxus.prisons.debug.commands;

import net.ultragrav.command.UltraCommand;
import org.bukkit.Bukkit;

public class DebugWorldId extends UltraCommand {
    public DebugWorldId() {
        this.addAlias("worldid");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        tell(getPlayer().getWorld().getUID().toString() + "    " + getPlayer().getWorld().getName());
        boolean contains = Bukkit.getServer().getWorlds().contains(getPlayer().getWorld());
        tell("&aContained: &f" + contains);
    }
}
