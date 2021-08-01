package com.soraxus.prisons.profiles.cmd;

import com.soraxus.prisons.profiles.PrisonProfile;
import com.soraxus.prisons.profiles.ProfileManager;
import com.soraxus.prisons.profiles.gui.MenuProfile;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

public class CmdProfile extends SpigotCommand {
    public CmdProfile() {
        this.addAlias("profile");
        this.addParameter(null, StringProvider.getInstance(), "player");
        this.setAllowConsole(false);
    }

    @Override
    protected void perform() {
        String name = getArgument(0);
        if (name == null)
            name = getSpigotPlayer().getName();

        Player player = Bukkit.getPlayer(name);

        String finalName = name;
        ForkJoinPool.commonPool().submit(() -> {
            try {
                UUID id;
                String name1;
                if (player == null) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(finalName);
                    if (p == null) {
                        tell("&cPlayer doesn't exist??");
                        return;
                    }
                    name1 = p.getName();
                    id = p.getUniqueId();
                } else {
                    name1 = player.getName();
                    id = player.getUniqueId();
                }

                PrisonProfile profile = ProfileManager.instance.loadProfile(id).join();
                if (profile == null) {
                    tell("&cPlayer does not have a profile!");
                    return;
                }

                new MenuProfile(getPlayer().getUniqueId(), name1, profile).open(getSpigotPlayer());

            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
