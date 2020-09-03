package com.soraxus.prisons.selling.mutlipliers.command;

import com.soraxus.prisons.core.command.GravCommand;
import com.soraxus.prisons.selling.mutlipliers.Multiplier;
import com.soraxus.prisons.selling.mutlipliers.MultiplierManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandMultiplier extends GravCommand {
    @Override
    public String getDescription() {
        return "Do shit with multipliers";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<String>() {{ add("multiplier"); add("multi"); }};
    }

    @Override
    public String getPermission() {
        return "prisoncore.multiplier";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage:");
            sender.sendMessage("§7> multi give <Player> <Multiplier> <Length (s)> > Give a player a multiplier item");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("prisoncore.multiplier.give")) {
                sender.sendMessage("§cYou do not have permission to use that command.");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("§cInvalid player: " + args[1]);
                return true;
            }

            double multi;
            try {
                multi = Double.parseDouble(args[2]);
            } catch(NumberFormatException e) {
                sender.sendMessage("§cInvalid number: " + args[2]);
                return true;
            }

            int length;
            try {
                length = Integer.parseInt(args[3]);
            } catch(NumberFormatException e) {
                sender.sendMessage("§cInvalid integer: " + args[3]);
                return true;
            }

            Multiplier multiplier = new Multiplier(length * 1000, multi);
            ItemStack item = MultiplierManager.instance.getItem(multiplier);
            player.getInventory().addItem(item);
            sender.sendMessage("§aGave " + player.getName() + " a " + multi + "x " + multiplier.getLengthStr() + " Multiplier!");
            return true;
        }
        return true;
    }
}
