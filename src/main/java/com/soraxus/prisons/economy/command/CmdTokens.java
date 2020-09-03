package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;

public class CmdTokens extends EconomyCheckCommand {
    public CmdTokens() {
        super(Economy.tokens);
        this.addAlias("tokens");
    }
}
