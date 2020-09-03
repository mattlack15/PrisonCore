package com.soraxus.prisons.worldedit.cmd;

import com.soraxus.prisons.worldedit.WorldEditPlayerManager;
import net.ultragrav.command.UltraCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdWand extends UltraCommand {
    public CmdWand() {
        this.addAlias("wand");
        this.setAllowConsole(false);
    }

    public void perform() {
        Player player = getPlayer();
        ItemStack stack = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(WorldEditPlayerManager.instance.getWand());
        player.getInventory().addItem(stack);
    }
}
