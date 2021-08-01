package com.soraxus.prisons.selling.autosell.command;

import com.soraxus.prisons.selling.ModuleSelling;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.StringProvider;

public class AutoSellCmd extends SpigotCommand {
    public AutoSellCmd() {
        this.addAlias("autosell");
        this.addAlias("as");

        this.setAllowConsole(false);
        this.setRequirePermission(false);

        this.addParameter("", StringProvider.getInstance(), "on/off");
    }

    @Override
    protected void perform() {
        String state = getArgument(0);
        boolean newState;
        if (state.isEmpty()) {
            newState = !ModuleSelling.instance.getAutoSellManager().getInfo(getPlayer().getUniqueId()).isEnabled();
        } else {
            newState = state.equalsIgnoreCase("on");
        }
        ModuleSelling.instance.getAutoSellManager().getInfo(getPlayer().getUniqueId()).setEnabled(newState);
        tell("&d&lAuto Sell > &7Autosell " + (newState ? "§aenabled" : "§cdisabled"));
    }
}
