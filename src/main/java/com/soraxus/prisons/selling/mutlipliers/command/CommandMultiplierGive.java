package com.soraxus.prisons.selling.mutlipliers.command;

import com.soraxus.prisons.selling.mutlipliers.Multiplier;
import com.soraxus.prisons.selling.mutlipliers.MultiplierManager;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.DoubleProvider;
import net.ultragrav.command.provider.impl.IntegerProvider;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandMultiplierGive extends UltraCommand {
    public CommandMultiplierGive() {
        addAlias("give");

        addParameter(PlayerProvider.getInstance());
        addParameter(DoubleProvider.getInstance(), "Multiplier");
        addParameter(IntegerProvider.getInstance(), "Length (s)");
    }

    public void perform() {
        Player player = getArgument(0);
        double multi = getArgument(1);
        int length = getArgument(3);
        Multiplier multiplier = new Multiplier(length * 1000, multi);
        ItemStack item = MultiplierManager.instance.getItem(multiplier);
        player.getInventory().addItem(item);
        tell("§aGave " + player.getName() + " a " + multi + "x " + multiplier.getLengthStr() + " Multiplier!");
    }
}
