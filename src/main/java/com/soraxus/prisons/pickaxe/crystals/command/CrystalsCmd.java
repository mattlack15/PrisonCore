package com.soraxus.prisons.pickaxe.crystals.command;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;

public class CrystalsCmd extends SpigotCommand {
    public CrystalsCmd() {
        this.addAlias("crystals");
        this.addAlias("crystal");

        this.addChildren(
                new CrystalsGiveCmd()
        );
    }
}
