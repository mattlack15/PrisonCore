package com.soraxus.prisons.cells;

import com.soraxus.prisons.cells.cmd.CmdCell;
import com.soraxus.prisons.cells.minions.Minion;
import com.soraxus.prisons.cells.minions.MinionItems;
import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.locks.CustomLock;
import com.soraxus.prisons.util.menus.MenuElement;
import net.md_5.bungee.api.chat.TextComponent;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.utils.IntVector3D;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModuleCells extends CoreModule {

    public static final long CELL_COST = 1000000;

    public static ModuleCells instance;

    private final CustomLock bypassModeLock = new CustomLock(true);
    private final List<UUID> bypassMode = new ArrayList<>();

    @Override
    protected void onEnable() {
        instance = this;
        File defaultSchem = new File(this.getDataFolder(), "defaultCell.bschem");
        Schematic cellSchem = null;
        if (defaultSchem.exists()) {
            try {
                cellSchem = new Schematic(defaultSchem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new CellManager(new File(this.getDataFolder(), "cells"), cellSchem);
        new CmdCell().register();
    }

    @Override
    protected void onDisable() {
        Synchronizer.finishRunnables();
        CellManager.instance.getLoadedCells().forEach(c -> {
            try {
                CellManager.instance.getSaveUnloadOp(c).run(); //Synchronously unload cuz we are in the main thread
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getName() {
        return "Cells";
    }

    public static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&d&lCells &f&l> &f");

    public static void messagePlayer(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void messagePlayer(Player player, TextComponent component) {
        TextComponent component1 = new TextComponent(TextComponent.fromLegacyText(PREFIX));
        component1.addExtra(component);
        player.spigot().sendMessage(component1);
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    public void setBypassMode(UUID id, boolean value) {
        bypassModeLock.perform(() -> {
            if (value) {
                if (!bypassMode.contains(id))
                    bypassMode.add(id);
            } else {
                bypassMode.remove(id);
            }
        });
    }

    public boolean getBypassMode(UUID id) {
        return bypassModeLock.perform(() -> bypassMode.contains(id));
    }

    @EventSubscription
    private void onLeave(PlayerQuitEvent event) {
        Cell cell = CellManager.instance.getLoadedCell(event.getPlayer().getUniqueId());
        if (cell != null) CellManager.instance.saveAndUnloadCell(cell);
    }

    //Minion placement
    @EventSubscription
    private void onMinionPlacement(BlockPlaceEvent event) {
        if (MinionItems.isValid(event.getItemInHand())) {
            Cell cell = null;
            for (Cell cell1 : CellManager.instance.getLoadedCells()) {
                if (event.getBlock().getWorld().equals(cell1.getWorld().getBukkitWorld())) {
                    cell = cell1;
                    break;
                }
            }
            if (cell == null) {
                event.setCancelled(true);
                return;
            }
            if (!cell.getPlayer().equals(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            if (!cell.getMinionManager().canPlace(IntVector3D.fromBukkitVector(event.getBlock().getLocation().toVector()))) {
                event.setCancelled(true);
                return;
            }
            Minion minion = cell.getMinionManager().createMinion(IntVector3D.fromBukkitVector(event.getBlock().getLocation().toVector()));
            MinionItems.MinionItemData data = MinionItems.getData(event.getItemInHand());

            if (data == null)
                return;

            minion.setMiningBlock(data.getType());
            minion.setName(data.getName());
            minion.setSpeed(data.getSpeed());
            minion.setCreator(event.getPlayer().getUniqueId());
            minion.setSettings(data.getSettings());
            minion.getSettings().setSkullName(event.getPlayer().getName());
            minion.spawn();
            event.setCancelled(true);
            ItemStack s = event.getPlayer().getInventory().getItemInMainHand();
            if (s == null)
                return;
            int amount = s.getAmount();
            if (amount == 0)
                return;
            amount--;
            s.setAmount(amount);
            event.getPlayer().getInventory().setItemInMainHand(s);
        }
    }
}
