package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import com.soraxus.prisons.worldedit.WorldEditPlayerState;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.IntegerProvider;
import org.bukkit.entity.Player;

public class CmdRotate extends SpigotCommand {
    public CmdRotate() {
        this.addAlias("rotate");

        this.setAllowConsole(false);

        this.addParameter(IntegerProvider.getInstance(), "rotation (0-3)");
    }

    public void perform() {
        int rot = getArgument(0);

        WorldEditPlayerState state = WorldEditPlayerManager.instance.getPlayerState(((Player) sender).getUniqueId());

        state.setClipboard(state.getClipboard().rotate(rot));

        getSpigotPlayer().sendMessage("§aRotated!");
    }
}
