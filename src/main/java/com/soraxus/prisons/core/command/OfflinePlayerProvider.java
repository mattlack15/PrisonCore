package com.soraxus.prisons.core.command;

import lombok.Getter;
import lombok.NonNull;
import net.ultragrav.command.exception.CommandException;
import net.ultragrav.command.provider.UltraProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OfflinePlayerProvider extends UltraProvider<OfflinePlayer> {
    @Getter
    private static final OfflinePlayerProvider instance = new OfflinePlayerProvider();

    private OfflinePlayerProvider() {
    }

    @Override
    public OfflinePlayer convert(@NonNull String s) throws CommandException {
        Player player = Bukkit.getPlayer(s); // Much faster in case the player is already online
        if (player != null) {
            return player;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(s);
        if (op == null) {
            throw new CommandException("§cPlayer: " + s + " has not joined the server");
        }
        return op;
    }

    /**
     * This completes only online players (for now)
     *
     * @param s String to complete
     * @return List of possibilities
     */
    @Override
    public List<String> tabComplete(@NonNull String s) {
        List<String> ret = new ArrayList<>();
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getName().toLowerCase().startsWith(s)) {
                ret.add(pl.getName());
            }
        }
        return ret;
    }

    @Override
    public String getArgumentDescription() {
        return "player";
    }
}
