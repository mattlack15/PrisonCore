package com.soraxus.prisons.mines.cmd;

import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.StringProvider;

public class CmdRenameMine extends UltraCommand {
    public CmdRenameMine() {
        this.addAlias("renamemine");
        this.addParameter(StringProvider.getInstance(), "current name");
        this.addParameter(StringProvider.getInstance(), "new name");
    }

    @Override
    protected void perform() {
        String current = getArgument(0);
        String next = getArgument(1);
        Mine mine = MineManager.instance.get(current);
        if(mine == null) {
            tell("&cCould not find mine &f" + current);
            return;
        }
        Mine.renameMine(mine, MineManager.instance, next);
        tell("&aIt should be renamed to " + next + " now.. maybe... then again maybe it didn't work :/ but hopefully it did :)");
    }
}
