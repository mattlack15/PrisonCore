package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;

public class CmdStars extends EconomyCheckCommand {
    public CmdStars() {
        super(Economy.stars);
        this.addAlias("stars");
    }
}
