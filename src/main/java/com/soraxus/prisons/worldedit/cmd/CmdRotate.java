package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.core.command.GravSubCommand;
import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import com.soraxus.prisons.worldedit.WorldEditPlayerState;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.IntegerProvider;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdRotate extends UltraCommand {
    public CmdRotate() {
        this.addAlias("rotate");

        this.setAllowConsole(false);

        this.addParameter(IntegerProvider.getInstance(), "rotation (0-3)");
    }

    public void perform() {
        int rot = getArgument(0);

        WorldEditPlayerState state = WorldEditPlayerManager.instance.getPlayerState(((Player) sender).getUniqueId());

        state.setClipboard(state.getClipboard().rotate(rot));

        sender.sendMessage("§aRotated!");
    }
}
