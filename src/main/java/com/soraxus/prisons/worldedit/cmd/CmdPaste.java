package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import com.soraxus.prisons.worldedit.WorldEditPlayerState;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CmdPaste extends SpigotCommand {
    public CmdPaste() {
        this.addAlias("paste");

        this.setAllowConsole(false);
    }

    private final ExecutorService service = Executors.newCachedThreadPool((r) -> new Thread(r, "AsyncWorld CmdPaste"));

    @Override
    public void perform() {
        if (!sender.hasPermission("asyncworld.paste")) {
            returnTell(SpigotPrisonCore.PREFIX + "&cYou don't have permission to do this!");
        }
        Player player = (Player) sender;

        WorldEditPlayerState state = WorldEditPlayerManager.instance.getPlayerState(player.getUniqueId());
        Schematic schem = state.getClipboard();

        service.submit(() -> {
            AsyncWorld world = new SpigotAsyncWorld(player.getLocation().getWorld());
            long ms = System.currentTimeMillis();
            world.pasteSchematic(schem, IntVector3D.fromBukkitVector(player.getLocation().toVector()));
            player.sendMessage(SpigotPrisonCore.PREFIX + "Pasted in memory in " + ((System.currentTimeMillis() - ms) / 1000D) + "s");
            world.flush().thenAccept((vo) -> {
                double time = (System.currentTimeMillis() - ms) / 1000D;
                player.sendMessage(SpigotPrisonCore.PREFIX + "Pasted in " + time + "s");
            });
            player.sendMessage(SpigotPrisonCore.PREFIX + "Flushed in memory in " + ((System.currentTimeMillis() - ms) / 1000D) + "s");
        });
    }
}
