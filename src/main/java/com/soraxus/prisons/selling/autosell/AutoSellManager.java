package com.soraxus.prisons.selling.autosell;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.event.PrisonPreSellEvent;
import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.selling.mutlipliers.MultiplierInfo;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.UltraCollectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class AutoSellManager {
    private ModuleSelling parent;
    public AutoSellManager(ModuleSelling parent) {
        this.parent = parent;
    }

    private Map<UUID, com.soraxus.prisons.selling.autosell.AutoSellInfo> infos = new HashMap<>();
    private static final long sellInterval = 30*1000;

    public AutoSellInfo getInfo(Player player) {
        return getInfo(player.getUniqueId());
    }
    public AutoSellInfo getInfo(UUID id) {
        if (!infos.containsKey(id)) {
            infos.put(id, new AutoSellInfo());
        }
        return infos.get(id);
    }

    public boolean isAutosell(UUID id) {
        return getInfo(id).isEnabled();
    }

    public void enableAutosell(UUID id) {
        getInfo(id).setEnabled(true);
        getInfo(id).setLastMessage(System.currentTimeMillis());
    }

    public long autosellNow(Player player, ItemStack item) {
        com.soraxus.prisons.selling.autosell.AutoSellInfo info = getInfo(player.getUniqueId());
        if(item == null)
            return 0;

        long tot = parent.getPrice(item);
        final double multi = new MultiplierInfo(player).getTotal();
        PrisonPreSellEvent event = new PrisonPreSellEvent(player, Collections.singletonList(item), multi);
        Bukkit.getPluginManager().callEvent(event);
        Economy.money.addBalance(player.getUniqueId(), (long) (tot * event.getMultiplier()));

        info.setTotal((long) (info.getTotal() + tot * multi));

        if (info.getLastMessage() + sellInterval < System.currentTimeMillis()) {
            player.sendMessage("§5§l            SELL SUMMARY");
            player.sendMessage("§8§m------------------------");
            player.sendMessage("§dMultiplier: §f" + event.getMultiplier());
            player.sendMessage("§dTotal: §f" + NumberUtils.formatFull(info.getTotal()));
            player.sendMessage("§8§m------------------------");
            info.setTotal(0);
            info.setLastMessage(System.currentTimeMillis());
        }

        return tot;
    }
}