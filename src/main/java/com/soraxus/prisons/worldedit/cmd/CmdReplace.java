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
import java.util.concurrent.atomic.AtomicInteger;

public class CmdReplace extends SpigotCommand {
    public CmdReplace() {
        addAlias("replace");
        setAllowConsole(false);

        addParameter(MaterialDataProvider.getInstance());
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
        int repl = getArgument(1);

        service.submit(() -> {
            long ms = System.currentTimeMillis();
            SpigotAsyncWorld world = new SpigotAsyncWorld(region.getWorld());
            AtomicInteger blocks = new AtomicInteger();
            world.asyncForAllInRegion(region, (loc, id, tagCompound, light) -> {
                if (id == mat) {
                    world.setBlock(loc, repl & 4095, (byte)(repl >>> 12));
                    blocks.incrementAndGet();
                }
            }, true);
            world.flush().thenAccept((Void) -> {
                double time = (System.currentTimeMillis() - ms) / 1000D;
                tell(SpigotPrisonCore.PREFIX + "&aReplaced " + NumberUtils.formatFull(blocks.get()) + " blocks in " + time + "s!");
            });
        });
    }
}
