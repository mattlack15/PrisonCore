package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import com.soraxus.prisons.worldedit.WorldEditPlayerState;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.utils.CuboidRegion;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CmdSet extends SpigotCommand {
    public CmdSet() {
        addAlias("set");
        setAllowConsole(false);

        addParameter(MaterialDataProvider.getInstance());
    }

    private ExecutorService service = Executors.newSingleThreadExecutor();

    public void perform() {
        WorldEditPlayerState state = WorldEditPlayerManager.instance.getPlayerState(((Player) sender).getUniqueId());

        if (state.getPos1() == null || state.getPos2() == null) {
            returnTell(SpigotPrisonCore.PREFIX + "Please make a valid selection!");
        }

        CuboidRegion region = new CuboidRegion(state.getPos1(), state.getPos2());
        int mat = getArgument(0);

        service.submit(() -> {
            long ms = System.currentTimeMillis();
            SpigotAsyncWorld world = new SpigotAsyncWorld(region.getWorld());
            long setMs = System.currentTimeMillis();
            world.setBlocks(region, () -> (short) mat);
            setMs = System.currentTimeMillis() - setMs;
            long flushMs = System.currentTimeMillis();
            world.flush(false).thenAccept((Void) -> {
                double time = (System.currentTimeMillis() - ms) / 1000D;
                int blocks = region.getArea();
                tell(SpigotPrisonCore.PREFIX + "&aFilled " + NumberUtils.formatFull(blocks) + " blocks in " + time + "s!");
            });
            flushMs = System.currentTimeMillis() - flushMs;
            tell(SpigotPrisonCore.PREFIX + "&e(" + setMs + ", " + flushMs + ")");
        });
    }
}
