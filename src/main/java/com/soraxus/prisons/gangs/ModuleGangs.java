package com.soraxus.prisons.gangs;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.gangs.cmd.CmdGang;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class ModuleGangs extends CoreModule {

    public static ModuleGangs instance;

    public ModuleGangs() {
        instance = this;
    }

    @Override
    protected void onEnable() {
        // Loading
        new GangMemberManager(new File(getDataFolder(), "players"));
        new GangRelationsManager(new File(getDataFolder(), "relations.yml"));
        GangRelationsManager.instance.load();
        new GangManager(new File(getDataFolder(), "gangs"));

        // Command
        new CmdGang().register();

        Bukkit.getOnlinePlayers().forEach(p -> GangMemberManager.instance.getOrMakeMember(p.getUniqueId(), p.getName()));
    }

    @Override
    protected void onDisable() {
        //Saving
        GangRelationsManager.instance.save();
    }

    @Override
    public String getName() {
        return "Gangs";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @EventSubscription
    private void onJoin(PlayerJoinEvent event) {
        GangMemberManager.instance.getOrMakeMember(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        getAsyncExecutor().submit(() -> {
            UUID gangId = GangMemberManager.instance.getMember(playerId).getGang();
            Gang gang = GangManager.instance.getLoadedGang(gangId);
            long onlineCount = gang.getMembers().stream()
                    .map(GangMember::getMember)
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .count();
            if (onlineCount == 0) {
                for (GangMember member : gang.getMembers()) {
                    GangMemberManager.instance.unloadMember(member.getMember());
                }
                GangManager.instance.unload(gangId);
            }
        });
    }
}