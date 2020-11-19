package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.bunkers.gui.MenuBunker;
import com.soraxus.prisons.gangs.Gang;
import org.bukkit.entity.Player;

public class CmdGangBunker extends GangCommand {
    public CmdGangBunker() {
        this.addAlias("bunker");
        this.setRequiresGang(true);
    }

    public void perform() {
        Gang gang = getGang();
        try {
            new MenuBunker(gang).open((Player) sender);
        } catch (Exception e) {
            e.printStackTrace();
            returnTell("&cCould not setup GUI for you :( sorry!");
        }
    }
}
