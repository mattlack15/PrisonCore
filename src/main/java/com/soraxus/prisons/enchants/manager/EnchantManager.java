package com.soraxus.prisons.enchants.manager;

import com.soraxus.prisons.enchants.ModuleEnchants;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.util.reflection.ReflectionUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EnchantManager {
    public static EnchantManager instance;

    private List<AbstractCE> enchantments = new ArrayList<>();

    private int id = 200;

    public EnchantManager() {
        instance = this;
    }

    /**
     * Get the current id and increment it
     * Used to generate a unique id for each enchantment
     * Initial id is set to 69420 to avoid conflicts with existing enchantments
     *
     * @return ID before incrementing
     */
    public synchronized int getAndIncrementId() {
        return id++;
    }

    /**
     * Get the folder in which enchant's config files reside
     */
    public File getEnchantConfigFolder() {
        return new File(ModuleEnchants.instance.getDataFolder(), "enchants");
    }

    public List<AbstractCE> getEnchantments() {
        return this.enchantments;
    }

    /**
     * Get an enchant by name
     */
    public synchronized AbstractCE getCE(String name) {
        for (AbstractCE enchant : enchantments) {
            if (enchant.getName().equalsIgnoreCase(name))
                return enchant;
        }
        return null;
    }

    public synchronized <T extends AbstractCE> AbstractCE getCE(Class<T> ce) {
        for (AbstractCE enchant : enchantments) {
            if (ce.isAssignableFrom(enchant.getClass()))
                return ce.cast(enchant);
        }
        return this.loadEnchant(ce);
    }

    /**
     * Get an enchant by display name
     */
    public synchronized AbstractCE getByDisplay(String name) {
        for (AbstractCE enchant : enchantments) {
            if (enchant.getDisplayName().equalsIgnoreCase(name))
                return enchant;
        }
        return null;
    }

    public String register(AbstractCE ce) {
        for (AbstractCE ces : enchantments) {
            if (ce.getName().equals(ces.getName()))
                return "Name or Display name clashes with another enchantment already loaded";
        }
        enchantments.add(ce);
        return null;
    }


    /**
     * Loads custom enchants from a package
     */
    public synchronized void loadEnchants(String packagePath, ClassLoader classLoader) {
        for (Class<?> clazz : ReflectionUtil.getClassesInPackage(packagePath, classLoader)) {
            if (!AbstractCE.class.isAssignableFrom(clazz)) {
                Bukkit.getLogger().warning("Class " + clazz.getSimpleName() + " is not an enchantment!");
                continue;
            }
            loadEnchant(clazz.asSubclass(AbstractCE.class));
        }
    }

    public synchronized AbstractCE loadEnchant(Class<? extends AbstractCE> clazz) {
        try {
            AbstractCE ce = clazz.newInstance();
            String response = register(ce);
            if (response != null) {
                Bukkit.getLogger().severe("Failed to load custom enchant " + ce.getName() + " due to: " + response);
                return null;
            }
            ce.enable();
            Bukkit.getLogger().info("Loaded custom enchant: " + ce.getName());
            return ce;
        } catch (InstantiationException | IllegalAccessException e) {
            Bukkit.getLogger().severe("Class " + clazz.getSimpleName() + " must have a nullary constructor");
            e.printStackTrace();
        }
        return null;
    }
}