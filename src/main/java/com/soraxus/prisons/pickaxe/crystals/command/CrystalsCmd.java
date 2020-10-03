package com.soraxus.prisons.pickaxe.crystals.command;

import net.ultragrav.command.UltraCommand;

public class CrystalsCmd extends UltraCommand {
    public CrystalsCmd() {
        this.addAlias("crystals");
        this.addAlias("crystal");

        this.addChildren(
                new CrystalsGiveCmd()
        );
    }
}
