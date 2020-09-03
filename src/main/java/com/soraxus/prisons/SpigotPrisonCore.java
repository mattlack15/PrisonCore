package com.soraxus.prisons;

import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.core.CmdThreadDump;
import com.soraxus.prisons.core.CmdPrisonCoreGUI;
import com.soraxus.prisons.core.CorePlugin;
import com.soraxus.prisons.crate.ModuleCrates;
import com.soraxus.prisons.economy.command.CmdEco;
import com.soraxus.prisons.economy.command.CmdMoney;
import com.soraxus.prisons.economy.command.CmdStars;
import com.soraxus.prisons.economy.command.CmdTokens;
import com.soraxus.prisons.enchants.ModuleEnchants;
import com.soraxus.prisons.gangs.ModuleGangs;
import com.soraxus.prisons.luckyblocks.ModuleLuckyBlocks;
import com.soraxus.prisons.mines.ModuleMines;
import com.soraxus.prisons.pickaxe.ModulePickaxe;
import com.soraxus.prisons.pluginhooks.ModulePluginHooks;
import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.data.PlayerData;
import com.soraxus.prisons.worldedit.ModuleWorldEdit;
import net.ultragrav.asyncworld.GlobalChunkQueue;
import net.ultragrav.serializer.GravSerializable;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpigotPrisonCore extends CorePlugin {

    public static SpigotPrisonCore instance;

    public static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&d&lSoraxus Prisons > &f");

    public SpigotPrisonCore() {
        super("spc", PREFIX);
    }

    @Override
    public void onEnable() {
        instance = this;
        GlobalChunkQueue.instance = new GlobalChunkQueue(this);
        this.init();
        this.addSubCommand(new CmdPrisonCoreGUI());

        GravSerializable.relocationMappings.put("com.soraxus.prisons.util.world.IntVector3D", "net.ultragrav.utils.IntVector3D");

        new EventSubscriptions();
        EventSubscriptions.instance.subscribe(this);
        this.addModule(new ModuleEnchants());
        this.addModule(new ModuleMines());
        this.addModule(new ModuleSelling());
        this.addModule(new ModuleBreak());
        this.addModule(new ModuleCrates());
        this.addModule(new ModulePluginHooks());
        this.addModule(new ModuleLuckyBlocks());
        this.addModule(new ModulePickaxe());
        this.addModule(new ModuleBunkers());
        this.addModule(new ModuleGangs());
        this.addModule(new ModuleWorldEdit());

        new CmdThreadDump().register();

        new CmdEco().register();
        new CmdMoney().register();
        new CmdStars().register();
        new CmdTokens().register();
    }

    @Override
    public void onDisable() {
        PlayerData.deinit();
        this.clearModules();
    }

    @EventSubscription
    public void onJoin(PlayerJoinEvent e) {
        PlayerData.set(e.getPlayer().getUniqueId(), "name", e.getPlayer().getName());
    }

    @EventSubscription
    public void onLeave(PlayerQuitEvent e) {
        PlayerData.unloadPlayerData(e.getPlayer().getUniqueId());
    }
}