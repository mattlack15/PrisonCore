package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import com.soraxus.prisons.worldedit.WorldEditPlayerState;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CmdLoadSchematic extends UltraCommand {
    public CmdLoadSchematic() {
        addAlias("loadschem");

        setAllowConsole(false);

        addParameter(StringProvider.getInstance(), "schematic");
    }

    @Override
    public void perform() {
        if (!sender.hasPermission("asyncworld.loadschem")) {
            returnTell(SpigotPrisonCore.PREFIX + "&cYou don't have permission to do this!");
        }
        Player player = getPlayer();

        String name = getArgument(0);
        File f = new File(ModuleBunkers.instance.getDataFolder(), "schematics/" + name + ".bschem");
        if (!f.exists()) {
            returnTell("§cThat schematic does not exist");
        }

        WorldEditPlayerState state = WorldEditPlayerManager.instance.getPlayerState(player.getUniqueId());

        try {
            Schematic schem = new Schematic(f);
            state.setClipboard(schem);
            tell("§aSchematic loaded successfully!");
        } catch(IOException e) {
            tell("§cThe schematic could not be loaded:");
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                tell("§c" + stackTraceElement.toString());
            }
        }
    }
}
