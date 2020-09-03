package com.soraxus.prisons.selling.mutlipliers;

import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.string.TextUtil;
import com.soraxus.prisons.util.UltraCollectors;
import com.soraxus.prisons.util.string.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MultiplierManager {
    public static MultiplierManager instance;

    private Map<UUID, Multiplier> multis = new HashMap<>();

    public MultiplierManager() {
        instance = this;
    }

    public void startLoop() {
        Scheduler.scheduleSyncRepeatingTask(() -> {
            if (multis.isEmpty()) {
                return;
            }
            multis = multis.entrySet().stream()
                    .filter(e -> {
                        Player player = Bukkit.getPlayer(e.getKey());
                        assert player != null;
                        if (!e.getValue().isActive()) {
                            e.getValue().expire(player);
                            return false;
                        }
                        e.getValue().render(player);
                        return true;
                    })
                    .collect(UltraCollectors.toMap());
        }, 1, 1);
    }

    public double getMultiplier(UUID id) {
        Multiplier multi = multis.getOrDefault(id, null);
        if (multi == null) {
            return 0;
        }
        return multi.getMulti();
    }

    public boolean tryActivate(Player player, Multiplier multi) {
        if (multis.containsKey(player.getUniqueId())) {
            return false;
        }
        multis.put(player.getUniqueId(), multi);
        multi.start();
        return true;
    }

    public ItemStack getItem(Multiplier multiplier) {
        ConfigurationSection cfg = ModuleSelling.instance.getMultiplierData().getConfigurationSection("item");
        Material mat = Material.valueOf(cfg.getString("type"));
        String displayName = cfg.getString("displayName");
        displayName = PlaceholderUtil.replacePlaceholders(displayName, multiplier.getPlaceholders());
        displayName = TextUtil.color(displayName);
        List<String> lore = cfg.getStringList("lore");
        lore = PlaceholderUtil.replacePlaceholders(lore, multiplier.getPlaceholders());
        lore = TextUtil.color(lore);
        ItemStack item = new ItemBuilder(mat, 1)
                .setName(displayName)
                .addLore(lore)
                .build();
        return multiplier.addNBT(item);
    }
}