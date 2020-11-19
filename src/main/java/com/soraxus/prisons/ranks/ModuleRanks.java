package com.soraxus.prisons.ranks;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.ranks.gui.MenuModuleRanks;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.PluginFile;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;

public class ModuleRanks extends CoreModule {

    public static ModuleRanks instance;

    @PluginFile
    public static File FILE_RANKS = null;

    @Override
    public String getName() {
        return "Rank";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.NAME_TAG, 1).setName("&f&lPrison Ranks").addLore("&7Click to enter &f&lPrison Ranks").build())
                .setClickHandler((e, i) -> new MenuModuleRanks(backButton).open(e.getWhoClicked()));
    }

    @Override
    protected void onEnable() {

        instance = this;

        //Files
        FILE_RANKS = new File(this.getDataFolder(), "ranks.yml");
        createFiles(this.getClass());

        new RankupManager(this);
        RankupManager.instance.loadRanks();
        RankupManager.instance.transferPRX();
        RankupManager.instance.saveRanks();

        Bukkit.getOnlinePlayers().forEach(p -> RankupManager.instance.loadPlayer(p.getUniqueId()));
    }

    @Override
    protected void onDisable() {
        RankupManager.instance.saveRanks();
    }

    @EventSubscription
    private void onJoin(PlayerJoinEvent event) {
        RankupManager.instance.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        RankupManager.instance.savePlayer(event.getPlayer().getUniqueId());
        RankupManager.instance.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
