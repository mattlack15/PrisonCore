package com.soraxus.prisons.profiles;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.profiles.cmd.CmdProfile;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ModuleProfiles extends CoreModule {

    @Override
    protected void onEnable() {
        String host = getConfig().getString("db_host", "");
        String database = getConfig().getString("db_database", "");
        int port = getConfig().getInt("db_port", 3306);
        String username = getConfig().getString("db_username", "");
        String password = getConfig().getString("db_password", "");
        new ProfileManager(host, database, port, username, password);
        new CmdProfile().register();
    }

    @Override
    protected void onDisable() {
        ProfileManager.instance.flushUpdateQueue();
    }

    @Override
    public String getName() {
        return "Profiles";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @EventSubscription
    private void onPreJoin(AsyncPlayerPreLoginEvent event) {
        ProfileManager.instance.loadOrCreateProfile(event.getUniqueId()).join();
    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        ProfileManager.instance.unloadProfile(event.getPlayer().getUniqueId());
    }
}
