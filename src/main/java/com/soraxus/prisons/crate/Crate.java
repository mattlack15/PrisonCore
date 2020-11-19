package com.soraxus.prisons.crate;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.crate.gui.MenuOpenCrate;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.items.NBTUtils;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.string.TextUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Crate {
    private static final String IDENTIFIER = "scrate";

    private String name;
    @Setter
    private String displayName;
    @Setter
    private String description;
    private List<Reward> rewards;
    @Setter
    private Material itemMaterial;
    @Setter
    private byte itemData;
    @Setter
    private int order;

    public static Crate fromSection(ConfigurationSection section) {
        String name = section.getName();
        String displayName = section.getString("display-name");
        String description = section.getString("description");
        Material material = Material.matchMaterial(section.getString("material"));
        int order = section.getInt("order", 0);
        byte matData = (byte) section.getInt("material-data");
        ConfigurationSection rewardSection = section.getConfigurationSection("rewards");
        List<Reward> rewards = new ArrayList<>();
        for (String key : rewardSection.getKeys(false)) {
            ConfigurationSection current = rewardSection.getConfigurationSection(key);
            rewards.add(Reward.fromSection(current));
        }
        return new Crate(name, displayName, description, rewards, material, matData, order);
    }

    public static Crate getCrate(ItemStack stack) {
        if (stack == null || stack.getType().equals(Material.AIR))
            return null;
        String name = NBTUtils.instance.getString(stack, IDENTIFIER);
        return CrateManager.instance.get(name);
    }

    public static ItemStack setCrate(ItemStack stack, Crate crate) {
        if (crate == null)
            return removeCrate(stack);
        if (stack == null || stack.getType().equals(Material.AIR))
            return null;
        return NBTUtils.instance.setString(stack, IDENTIFIER, crate.getName());
    }

    public Reward getReward() {
        double tot = rewards.stream().mapToDouble(Reward::getChance).sum();
        double rand = MathUtils.random(0, tot);
        double to = 0;
        for (Reward rew : rewards) {
            to += rew.getChance();
            if (to >= rand) {
                return rew;
            }
        }
        return null;
    }

    public void open(Player player) {
        MenuOpenCrate openAnim = new MenuOpenCrate(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                openAnim.open(player);
                if (!openAnim.iterate()) {
                    cancel();
                    openAnim.getFinalReward().apply(player);
                }
            }
        }.runTaskTimer(SpigotPrisonCore.instance, 0, 2);
    }

    //

    public static boolean hasCrate(ItemStack stack) {
        return getCrate(stack) != null;
    }

    public static ItemStack removeCrate(ItemStack stack) {
        if (stack == null || stack.getType().equals(Material.AIR))
            return null;
        return NBTUtils.instance.remove(stack, IDENTIFIER);
    }

    public ItemStack getItem() {
        ItemBuilder builder = new ItemBuilder(itemMaterial, 1, itemData);
        builder.setName(this.getDisplayName());
        for (String descLine : TextUtil.splitIntoLines(getDescription(), 30))
            builder.addLore("&7" + descLine);
        builder.addLore("&aClick the air to Open");
        ItemStack stack = builder.build();
        return setCrate(stack, this);
    }

    public void saveTo(ConfigurationSection section) {
        section.set("display-name", this.displayName);
        section.set("description", this.description);
        section.set("material", this.getItemMaterial().toString());
        section.set("material-data", (int) this.getItemData());
        section.set("order", this.order);
        ConfigurationSection rewardSection = section.createSection("rewards");
        int i = 0;
        for (Reward reward : rewards) {
            ConfigurationSection current = rewardSection.createSection(Integer.toString(i));
            reward.saveTo(current);
            i++;
        }
    }
}