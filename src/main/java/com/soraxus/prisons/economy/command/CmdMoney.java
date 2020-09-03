package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;

public class CmdMoney extends EconomyCheckCommand {
    public CmdMoney() {
        super(Economy.money);
        this.addAlias("balance");
        this.addAlias("bal");
        this.addAlias("money");
    }
}
