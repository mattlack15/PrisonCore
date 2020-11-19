package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import com.soraxus.prisons.worldedit.WorldEditPlayerState;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CmdCopy extends UltraCommand {
    public CmdCopy() {
        addAlias("copy");
        setAllowConsole(false);
    }

    private ExecutorService service = Executors.newSingleThreadExecutor();

    public void perform() {
        if (!sender.hasPermission("asyncworld.copy")) {
            returnTell(SpigotPrisonCore.PREFIX + "&cYou don't have permission to do this!");
        }
        if (!(sender instanceof Player)) {
            returnTell(SpigotPrisonCore.PREFIX + "&cYou must be a player to use this command");
        }

        WorldEditPlayerState state = WorldEditPlayerManager.instance.getPlayerState(((Player) sender).getUniqueId());

        if (state.getPos1() == null || state.getPos2() == null) {
            returnTell(SpigotPrisonCore.PREFIX + "Please make a valid selection!");
        }

        CuboidRegion region = new CuboidRegion(state.getPos1(), state.getPos2());
        IntVector3D origin = new IntVector3D(0, 0, 0);

        service.submit(() -> {
            long ms = System.currentTimeMillis();
            Schematic schem = new SpigotAsyncWorld(region.getWorld()).optimizedCreateSchematic(region, origin, -1);
            double time = (System.currentTimeMillis() - ms) / 1000D;
            state.setClipboard(schem);
            tell(SpigotPrisonCore.PREFIX + "&aCopied in " + time + "s!");
        });
    }
}
