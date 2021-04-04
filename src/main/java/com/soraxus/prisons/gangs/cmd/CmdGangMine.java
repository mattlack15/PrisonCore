package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.GangRole;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.gui.MenuBrowseMines;
import com.soraxus.prisons.privatemines.gui.MenuPrivateMine;
import net.ultragrav.command.provider.impl.StringProvider;

public class CmdGangMine extends GangCommand {

    public CmdGangMine() {
        this.addAlias("mine");
        this.addParameter(null, StringProvider.getInstance(), "browse");
    }

    @Override
    protected void perform() {
        if (this.getArgument(0) != null) {
            new MenuBrowseMines(null).open(getPlayer());
        } else {
            if(getGangMember().getGang() == null) {
                tell("&cYou are not in a gang!");
                return;
            }
            if(getGangMember().getGangRole().ordinal() < GangRole.MOD.ordinal()) {
                tell("&cYou are not a high enough role in your gang to use this!");
                return;
            }
            if (getGang().getLevel() >= 5) {
                PrivateMine m = getGang().getMine();
                if (m == null) {
                    getAsyncExecutor().submit(() -> {
                        try {
                            PrivateMine mine = getGang().createOrLoadMine().join();
                            new MenuPrivateMine(mine).open(getPlayer());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                } else {
                    new MenuPrivateMine(m).open(getPlayer());
                }
            } else {
                tell(CmdGang.PREFIX + "&cYou're gang is not a high enough level to have a mine yet!");
                tell(CmdGang.PREFIX + "Are you looking for &a/gang mine browse?");
            }
        }
    }
}
