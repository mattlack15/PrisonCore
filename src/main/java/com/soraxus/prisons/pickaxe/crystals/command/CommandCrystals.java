package com.soraxus.prisons.pickaxe.crystals.command;

import com.soraxus.prisons.core.command.GravCommand;
import com.soraxus.prisons.pickaxe.crystals.Crystal;
import com.soraxus.prisons.pickaxe.crystals.CrystalType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandCrystals extends GravCommand {
    @Override
    public String getDescription() {
        return "Do shit with crystals";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<String>() {{ add("crystals"); add("crystal"); }};
    }

    @Override
    public String getPermission() {
        return "prisoncore.crystals";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage:");
            sender.sendMessage("§7> crystal give <Player> <Type> <Tier> > Give a player a crystal item");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("prisoncore.crystals.give")) {
                sender.sendMessage("§cYou do not have permission to use that command.");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("§cInvalid player: " + args[1]);
                return true;
            }

            CrystalType type;
            try {
                type = CrystalType.valueOf(args[2].toUpperCase());
            } catch(EnumConstantNotPresentException e) {
                sender.sendMessage("§cInvalid crystal type: " + args[2]);
                return true;
            }

            int tier;
            try {
                tier = Integer.parseInt(args[3]);
            } catch(NumberFormatException e) {
                sender.sendMessage("§cInvalid integer: " + args[3]);
                return true;
            }

            Crystal crystal = new Crystal(-1, type, tier);
            player.getInventory().addItem(crystal.getItem());
            sender.sendMessage("§aGave " + player.getName() + " a tier " + tier + " " + type.getDisplayName() + " crystal!");
            return true;
        }
        return true;
    }
}
