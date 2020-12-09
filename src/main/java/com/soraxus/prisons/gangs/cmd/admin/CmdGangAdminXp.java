package com.soraxus.prisons.gangs.cmd.admin;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.cmd.GangProvider;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.LongProvider;

public class CmdGangAdminXp extends UltraCommand {
    public CmdGangAdminXp() {
        this.addAlias("xp");
        this.addAlias("exp");

        this.addParameter(GangProvider.getInstance());
        this.addParameter(OperationProvider.getInstance());
        this.addParameter(LongProvider.getInstance());
    }

    @Override
    protected void perform() {
        Gang g = getArgument(0);
        XpOperation op = getArgument(1);
        long d = getArgument(2);

        g.setXp(op.apply(g.getXp(), d));
    }
}
