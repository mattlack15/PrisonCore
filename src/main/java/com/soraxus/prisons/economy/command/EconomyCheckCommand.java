package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.NumberUtils;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.entity.Player;
import net.ultragrav.command.platform.SpigotCommand;

public abstract class EconomyCheckCommand extends SpigotCommand {
    private Economy eco;

    public EconomyCheckCommand(Economy eco) {
        this.eco = eco;
        this.addParameter(null, PlayerProvider.getInstance());
    }

    public void perform() {
        Player player = getArgument(0);
        if (player == null) {
            if (isPlayer()) {
                player = getSpigotPlayer();
            } else {
                returnTell("§cYou must specify the player.");
                return;
            }
        } else {
            if (!getSender().hasPermission("economy.other")) {
                returnTell("§cYou do not have permission to execute this command.");
            }
        }

        tell(CmdEco.PREFIX + "You have " + eco.format(NumberUtils.toReadableNumber(eco.getBalance(player.getUniqueId()))));
    }
}
