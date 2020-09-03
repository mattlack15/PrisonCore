package com.soraxus.prisons.selling.autosell.command;

import com.soraxus.prisons.core.command.GravCommand;
import com.soraxus.prisons.selling.ModuleSelling;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandAutoSell extends GravCommand {
    @Override
    public String getDescription() {
        return "Toggles auto-selling";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<String>() {{ add("autosell"); }};
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player))
            return sendErrorMessage(sender, "&c&lYou must be a player to use this command!");

        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("on")) {
                ModuleSelling.instance.getAutoSellManager().getInfo(((Player)sender).getUniqueId()).setEnabled(true);
                sendErrorMessage(sender, "&d&lAuto Sell > &7Autosell &aenabled");
            } else {
                ModuleSelling.instance.getAutoSellManager().getInfo(((Player)sender).getUniqueId()).setEnabled(false);
                sendErrorMessage(sender, "&d&lAuto Sell > &7Autosell &cdisabled");

            }
        } else {
            if(ModuleSelling.instance.getAutoSellManager().getInfo(((Player)sender).getUniqueId()).isEnabled()) {
                ModuleSelling.instance.getAutoSellManager().getInfo(((Player)sender).getUniqueId()).setEnabled(false);
                sendErrorMessage(sender, "&d&lAuto Sell > &7Autosell &cdisabled");
            } else {
                ModuleSelling.instance.getAutoSellManager().getInfo(((Player)sender).getUniqueId()).setEnabled(true);
                sendErrorMessage(sender, "&9&lAuto Sell > &7Autosell &aenabled");
            }
        }
        return true;
    }
}
