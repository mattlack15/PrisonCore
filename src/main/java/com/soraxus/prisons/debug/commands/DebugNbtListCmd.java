package com.soraxus.prisons.debug.commands;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.ultragrav.command.UltraCommand;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class DebugNbtListCmd extends UltraCommand {
    public DebugNbtListCmd() {
        addAlias("list");

        setAllowConsole(false);
    }

    @Override
    protected void perform() {
        ItemStack item = getPlayer().getInventory().getItemInMainHand();

        NBTTagCompound compound = CraftItemStack.asNMSCopy(item).getTag();
        if (compound == null) {
            tell("§cThis item has no NBT");
            return;
        }
        Set<String> elements = compound.c();
        elements.forEach(element -> tell("§7- " + element + ": " + compound.get(element).toString()));
    }
}
