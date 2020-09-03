package com.soraxus.prisons.enchants;

import com.soraxus.prisons.enchants.manager.AbilityCooldownManager;
import com.soraxus.prisons.enchants.manager.EnchantManager;
import com.soraxus.prisons.util.EventSubscriptions;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotEnchants extends JavaPlugin {
    public static SpigotEnchants instance;

    @Override
    public void onEnable() {
        instance = this;

        new EventSubscriptions();

        new EnchantManager();
        new AbilityCooldownManager();

        EnchantManager.instance.loadEnchants("com.soraxus.prisons.enchants.customenchants", getClassLoader());
    }

    public void onDisable() {
    }
}