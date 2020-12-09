package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.GangManager;
import net.ultragrav.command.UltraCommand;

import java.util.List;

public class CmdGangList extends GangCommand {
    public CmdGangList() {
        this.addAlias("list");
        this.setRequiresGang(false);
        this.setAllowConsole(true);
    }

    @Override
    protected void perform() {
        List<String> gangs = GangManager.instance.listGangs();
        gangs.forEach(g -> tell(CmdGang.PREFIX + " &7- &f" + g));
    }
}
