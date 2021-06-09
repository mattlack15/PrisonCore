package com.soraxus.prisons;

import com.soraxus.prisons.announcements.ModuleAnnouncements;
import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.cells.ModuleCells;
import com.soraxus.prisons.shop.ModuleShop;
import com.soraxus.prisons.shop.customshop.CmdGlobalShop;
import com.soraxus.prisons.chatgames.ModuleChatGames;
import com.soraxus.prisons.core.CmdPrisonCoreGUI;
import com.soraxus.prisons.core.CmdThreadDump;
import com.soraxus.prisons.core.CorePlugin;
import com.soraxus.prisons.crate.ModuleCrates;
import com.soraxus.prisons.debug.ModuleDebug;
import com.soraxus.prisons.economy.ModuleEconomy;
import com.soraxus.prisons.economy.command.*;
import com.soraxus.prisons.enchants.ModuleEnchants;
import com.soraxus.prisons.errors.ModuleErrors;
import com.soraxus.prisons.gangs.ModuleGangs;
import com.soraxus.prisons.luckyblocks.ModuleLuckyBlocks;
import com.soraxus.prisons.mines.ModuleMines;
import com.soraxus.prisons.pickaxe.ModulePickaxe;
import com.soraxus.prisons.pluginhooks.ModulePluginHooks;
import com.soraxus.prisons.privatemines.ModulePrivateMines;
import com.soraxus.prisons.profiles.ModuleProfiles;
import com.soraxus.prisons.ranks.ModuleRanks;
import com.soraxus.prisons.ranks.cmd.CmdPrestige;
import com.soraxus.prisons.ranks.cmd.CmdRankup;
import com.soraxus.prisons.ranks.cmd.CmdRankupMax;
import com.soraxus.prisons.ranks.cmd.CmdSetRank;
import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.sorting.ModuleSorting;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.data.FileUtils;
import com.soraxus.prisons.util.data.PlayerData;
import com.soraxus.prisons.worldedit.ModuleWorldEdit;
import net.ultragrav.asyncworld.GlobalChunkQueue;
import net.ultragrav.serializer.GravSerializable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;

public class SpigotPrisonCore extends CorePlugin {

    public static SpigotPrisonCore instance;

    public static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&d&lSoraxus Prisons > &f");

    private String spawn;

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
        this.addModule(new ModuleErrors());
        this.addModule(new ModuleSorting());
        this.addModule(new ModuleEnchants());
        this.addModule(new ModuleMines());
        this.addModule(new ModuleSelling());
        this.addModule(new ModuleBreak());
        this.addModule(new ModuleCrates());
        this.addModule(new ModuleEconomy());
        this.addModule(new ModulePluginHooks());
        this.addModule(new ModuleLuckyBlocks());
        this.addModule(new ModulePickaxe());
        this.addModule(new ModuleBunkers());
        this.addModule(new ModulePrivateMines());
        this.addModule(new ModuleGangs());
        this.addModule(new ModuleWorldEdit());
        this.addModule(new ModuleCells());
        this.addModule(new ModuleProfiles());
        this.addModule(new ModuleRanks());
        this.addModule(new ModuleChatGames());
        this.addModule(new ModuleShop());
        this.addModule(new ModuleAnnouncements());
        this.addModule(new ModuleDebug());

        new CmdThreadDump().register();

        // \/ \/ These belong in their respective modules
        new CmdEco().register();
        new CmdMoney().register();
        new CmdStars().register();
        new CmdTokens().register();
        new CmdBankNote().register();
        new CmdRankup().register();
        new CmdRankupMax().register();
        new CmdPrestige().register();
        new CmdSetRank().register();
        new CmdGlobalShop().register();
        // /\ /\ These belong in their respective modules

        this.spawn = getConfig().getString("spawn");

        Synchronizer.class.getName();

        Diagnostics.runDiagnostics();
    }

    @Override
    public void onDisable() {
        Synchronizer.closeAndFinish();
        PlayerData.deinit();
        this.clearModules();

        BunkerManager.instance.lastTick = -2; //Make sure that the crash detector doesn't think that the server has crashed

        File in = new File(getDataFolder(), "Enchants-1.0.0.jar");
        File out = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (in.exists()) {
            try {
                FileUtils.moveAndOverwrite(in, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Location getSpawn() {
        String[] a = spawn.split(",");
        return new Location(Bukkit.getWorld(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2]),
                Double.parseDouble(a[3]));
    }

    @EventSubscription
    public void onJoin(PlayerJoinEvent e) {
        PlayerData.set(e.getPlayer().getUniqueId(), "name", e.getPlayer().getName());
    }

    @EventSubscription
    public void onLeave(PlayerQuitEvent e) {
        PlayerData.unloadPlayerData(e.getPlayer().getUniqueId());
    }

    @EventSubscription
    private void onHunger(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }
}