package com.soraxus.prisons.crate;

import com.soraxus.prisons.util.CastUtil;
import com.soraxus.prisons.util.items.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class Reward {
    @Setter
    @Getter
    private String description;
    @Getter
    private List<String> commands;
    @Getter
    private List<ItemStack> rewardItems;
    @Setter
    private ItemStack displayItem;
    @Setter
    @Getter
    private double chance;

    public static Reward fromSection(ConfigurationSection section) {
        List<String> commands = section.isList("commands") ? section.getStringList("commands") : new ArrayList<>();
        ItemStack stack = section.isItemStack("display-item") ? section.getItemStack("display-item") : null;
        List<ItemStack> toGive = CastUtil.cast(section.getList("reward-items"));

        double chance = section.getDouble("chance-percentage") / 100d;
        String name = section.getString("name");
        return new Reward(name, commands, toGive, stack, chance);
    }

    public ItemStack getDisplayItem() {
        return new ItemBuilder(this.displayItem).setName(this.getDescription()).addLore("&fChance: &a" + (chance * 100)).build();
    }

    public void saveTo(ConfigurationSection section) {
        if (commands.size() != 0) {
            section.set("commands", commands);
        }
        if (displayItem != null) {
            section.set("display-item", displayItem);
        }

        section.set("reward-items", rewardItems);
        section.set("name", description);
        section.set("chance-percentage", chance * 100d);
    }

    public void apply(Player player) {
        for (String str : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replaceAll("<player>", player.getName()));
        }
        for (ItemStack item : rewardItems) {
            player.getInventory().addItem(item);
        }
    }
}
