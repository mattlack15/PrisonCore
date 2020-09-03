package com.soraxus.prisons.pluginhooks;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.economy.VaultEconomyHook;
import com.soraxus.prisons.util.menus.MenuElement;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public class ModulePluginHooks extends CoreModule {
    private VaultEconomyHook econInstance;

    @Override
    public String getName() {
        return "PluginHooks";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        if (setupEconomy()) {
            // Any other vault hooks should go here
            // To avoid checking if it is loaded more than once
        } else {
            Bukkit.getLogger().severe("[PrisonCore] No Vault dependency found!");
        }
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            PlaceholderAPI.registerPlaceholderHook(getParent().getName(), new SPlaceholderHook()); // TODO Make sure to add this to the workspace
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        econInstance = new VaultEconomyHook();
        final Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        Bukkit.getServicesManager().register(Economy.class, econInstance, vault, ServicePriority.Highest);
        return true;
    }
}
