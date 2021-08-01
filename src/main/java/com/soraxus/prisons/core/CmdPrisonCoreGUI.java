package com.soraxus.prisons.core;

import com.soraxus.prisons.SpigotPrisonCore;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import org.bukkit.entity.Player;

public class CmdPrisonCoreGUI extends SpigotCommand {
    public CmdPrisonCoreGUI() {
        this.addAlias("gui");

        this.setAllowConsole(false);
    }

    public void perform() {
        SpigotPrisonCore.instance.getCoreGUI().open((Player) sender);
    }
}
