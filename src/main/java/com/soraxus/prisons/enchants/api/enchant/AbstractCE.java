package com.soraxus.prisons.enchants.api.enchant;

import com.soraxus.prisons.enchants.manager.EnchantManager;
import com.soraxus.prisons.util.CastUtil;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.items.NBTUtils;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCE {
    private static String ENCHANTMENT_IDENTIFIER = "spc.ce";

    private FileConfiguration config;

    private boolean enabled = false;

    @Getter
    private List<String> description;
    @Getter
    private int maxLevel;
    @Getter
    private String displayName;

    public AbstractCE() {
        getConfig();
    }

    public void enable() {
        if (this.isEnabled())
            return;
        EventSubscriptions.instance.subscribe(this);
        this.enabled = true;
        displayName = getConfig().getString("display-name");
        maxLevel = getConfig().getInt("max-level");
        description = getConfig().getStringList("description");
        this.onEnable();
    }

    public void disable() {
        if (!this.isEnabled())
            return;
        EventSubscriptions.instance.unSubscribe(this);
        this.enabled = false;
        this.onDisable();
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    public abstract long getCost(int level);

    public boolean isEnabled() {
        return this.enabled;
    }

    protected void reloadConfig() {
        this.getConfig(); //Make sure it's created
        this.config = YamlConfiguration.loadConfiguration(new File(EnchantManager.instance.getEnchantConfigFolder(), getName() + ".yml"));
    }

    protected FileConfiguration getConfig() {
        if (config != null)
            return config;
        if (!EnchantManager.instance.getEnchantConfigFolder().exists())
            EnchantManager.instance.getEnchantConfigFolder().mkdirs();
        File file = new File(EnchantManager.instance.getEnchantConfigFolder(), getName() + ".yml");
        if (!file.exists()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(file.getName());
            if (stream != null) {
                try {
                    OutputStream outputStream = new FileOutputStream(file);
                    byte[] bites = new byte[stream.available()];
                    stream.read(bites);
                    outputStream.write(bites);
                    outputStream.flush();
                    outputStream.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        return config;
    }

    protected void saveConfig() {
        try {
            this.config.save(new File(EnchantManager.instance.getEnchantConfigFolder(), getName() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EnchantInfo getInfo(ItemStack stack) {
        if (stack == null || stack.getType().equals(Material.AIR))
            return new EnchantInfo(new HashMap<>());

        Map<AbstractCE, Integer> enchants = new HashMap<>();
        getEnchantments(NBTUtils.instance.getByteArray(stack, ENCHANTMENT_IDENTIFIER)).forEach((e, l) -> {
            AbstractCE ce = EnchantManager.instance.getCE(e);
            if (ce != null)
                enchants.put(ce, l);
        });
        return new EnchantInfo(enchants);
    }

    public ItemStack enchant(ItemStack stack, int level) {
        if(stack == null)
            return null;
        stack = unEnchant(stack);
        Map<String, Integer> enchants = getEnchantments(NBTUtils.instance.getByteArray(stack, ENCHANTMENT_IDENTIFIER));
        enchants.put(this.getName(), level);
        ItemStack out = new ItemBuilder(NBTUtils.instance.setByteArray(stack, ENCHANTMENT_IDENTIFIER, createByteArrayFromEnchantments(enchants))).addLore(0, this.getIdentifier() + " &f" + level).build();
        onEnchant(out, level);
        return out;
    }

    public static ItemStack clearEnchant(ItemStack stack) {
        if(stack == null)
            return null;
        for (AbstractCE enchs : getInfo(stack).getEnchants().keySet()) {
            stack = enchs.unEnchant(stack);
        }
        return NBTUtils.instance.remove(stack, ENCHANTMENT_IDENTIFIER);
    }

    public ItemStack unEnchant(ItemStack stack) {
        if(stack == null)
            return null;
        Map<String, Integer> enchants = getEnchantments(NBTUtils.instance.getByteArray(stack, ENCHANTMENT_IDENTIFIER));
        int level = enchants.getOrDefault(this.getName(), 0);
        enchants.remove(this.getName());
        if (enchants.size() == 0) {
            stack = NBTUtils.instance.remove(stack, ENCHANTMENT_IDENTIFIER);
        }
        ItemStack out = new ItemBuilder(NBTUtils.instance.setByteArray(stack, ENCHANTMENT_IDENTIFIER, createByteArrayFromEnchantments(enchants))).removeLore(this.getIdentifier() + " &f" + level, true).build();
        onUnenchant(out);
        return out;
    }

    public String getIdentifier() {
        return ChatColor.translateAlternateColorCodes('&', getDisplayName());
    }

    public int getLevel(ItemStack stack) {
        if(stack == null)
            return 0;
        Map<String, Integer> enchants = getEnchantments(NBTUtils.instance.getByteArray(stack, ENCHANTMENT_IDENTIFIER));
        if (!enchants.containsKey(this.getName()))
            return 0;
        return enchants.get(this.getName());
    }

    public boolean hasEnchant(ItemStack stack) {
        if(stack == null)
            return false;
        Map<String, Integer> enchants = getEnchantments(NBTUtils.instance.getByteArray(stack, ENCHANTMENT_IDENTIFIER));
        return enchants.containsKey(this.getName());
    }

    public abstract String getName();

    public abstract int getStartLevel();

    public abstract EnchantmentTarget getItemTarget();

    public abstract boolean canEnchantItem(ItemStack itemStack);

    public static boolean containsAnEnchant(ItemStack stack) {
        return getInfo(stack).getEnchants().size() != 0;
    }

    public abstract void onUnenchant(ItemStack stack);

    public abstract void onEnchant(ItemStack stack, int level);

    public static Map<String, Integer> getEnchantments(byte[] bytes) {
        GravSerializer serializer = new GravSerializer(bytes);
        try {
            return CastUtil.cast((Map<?, ?>) serializer.readObject());
        } catch(IllegalStateException e) {
            return new HashMap<>();
        }
    }

    public static byte[] createByteArrayFromEnchantments(@NotNull Map<String, Integer> enchantments) {
        GravSerializer serializer = new GravSerializer();
        serializer.writeObject(enchantments);
        return serializer.toByteArray();
    }
}
