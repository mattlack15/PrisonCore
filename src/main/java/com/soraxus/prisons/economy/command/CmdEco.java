package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.LongProvider;
import net.ultragrav.command.provider.impl.StringProvider;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.entity.Player;

public class CmdEco extends UltraCommand {
    public static final String PREFIX = "&a&lEconomy > &7";

    public CmdEco() {
        this.addAlias("economy");
        this.addAlias("eco");

        this.addParameter(EconomyProvider.getInstance());
        this.addParameter(StringProvider.getInstance(), "action");
        this.addParameter(PlayerProvider.getInstance());
        this.addParameter(0L, LongProvider.getInstance(), "amount");
    }

    public void perform() {
        Economy economy = getArgument(0);
        String action = getArgument(1);
        Player player = getArgument(2);
        long amount = getArgument(3);

        switch (action.toLowerCase()) {
            case "give":
                economy.addBalance(player.getUniqueId(), amount);
                returnTell(PREFIX + player.getName() + "'s balance was increased by &e" + amount);
            case "set":
                economy.setBalance(player.getUniqueId(), amount);
                returnTell(PREFIX + player.getName() + "'s balance was set to &e" + amount);
            case "take":
                economy.removeBalance(player.getUniqueId(), amount);
                returnTell(PREFIX + player.getName() + "'s balance was lowered by &e" + amount);
            case "reset":
                amount = economy.resetBalance(player.getUniqueId());
                returnTell(PREFIX + player.getName() + "'s balance was reset to &e" + amount);
            default:
                returnTell(PREFIX + "Usage: /eco <token/money/star> <give/take/set/reset> <player> [amount]");
        }
    }
}
