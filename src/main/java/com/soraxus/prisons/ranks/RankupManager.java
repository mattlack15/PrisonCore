package com.soraxus.prisons.ranks;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.locks.CustomLock;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RankupManager {
    public static RankupManager instance;

    private final ModuleRanks parent;

    /**
     * An ordered list of ranks from lowest to highest
     */
    private final List<Rank> ranks = new ArrayList<>();
    private final Map<UUID, PRankPlayer> playerRanks = new HashMap<>();
    @Getter
    private final List<String> prestigeCmds = Collections.synchronizedList(new ArrayList<>());
    private final CustomLock lock = new CustomLock(true);

    public RankupManager(ModuleRanks parent) {
        this.parent = parent;
        getPlayerFile(UUID.randomUUID()).getParentFile().mkdirs();
        instance = this;
    }

    public void transferPRX() {
        File file = new File(new File(SpigotPrisonCore.instance.getDataFolder().getParentFile(), "PrisonRanksX"), "config.yml");
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

        List<Rank> rankList = new ArrayList<>();
        for (String name : conf.getConfigurationSection("Ranks").getKeys(false)) {
            ConfigurationSection section = conf.getConfigurationSection("Ranks." + name);
            Rank rank = new Rank(name, section.getString("display", name).replace("&fP&e%prisonranksx_prestige_name% ", ""), (long) section.getDouble("cost", 0D), section.getStringList("executecmds"));
            rankList.add(rank);
        }

        lock.lock();
        try {
            System.out.println("Imported " + rankList.size() + " prison ranks from PrisonRanksX!");
            ranks.clear();
            ranks.addAll(rankList);
            prestigeCmds.clear();
            prestigeCmds.addAll(conf.getStringList("prestigeOptions.prestige-cmds"));
            saveRanks();
        } finally {
            lock.unlock();
        }

    }


    //Rank related
    public void loadRanks() {
        lock.lock();
        try {
            File file = ModuleRanks.FILE_RANKS;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            ranks.clear();
            playerRanks.clear(); //Unload all players, they will be reloaded on-use

            if (config.isConfigurationSection("ranks")) {
                for (String name : config.getConfigurationSection("ranks").getKeys(false)) {
                    Rank rank = new Rank(config.getConfigurationSection("ranks." + name));
                    ranks.add(rank);
                }
            }

            this.prestigeCmds.clear();
            this.prestigeCmds.addAll(config.getStringList("prestige-cmds"));

        } finally {
            lock.unlock();
        }
    }

    public void saveRanks() {
        lock.lock();
        try {
            File file = ModuleRanks.FILE_RANKS;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("ranks", null);
            config.set("prestige-cmds", this.prestigeCmds);

            ranks.forEach(rank -> rank.serialize(config.createSection("ranks." + rank.getName())));

            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int getIndex(Rank rank) {
        return lock.perform(() -> this.ranks.indexOf(rank));
    }

    public Rank getRank(String name) {
        if (name == null)
            return null;
        return lock.perform(() -> {
            for (Rank rank : this.ranks) {
                if (rank.getName().equalsIgnoreCase(name))
                    return rank;
            }
            return null;
        });
    }

    public Rank getRank(int index) {
        return lock.perform(() -> {
            if (index >= this.ranks.size()) {
                return null;
            }
            return this.ranks.get(index);
        });
    }

    public List<Rank> getRanks() {
        return lock.perform(() -> new ArrayList<>(this.ranks));
    }

    public void addRank(Rank rank) {
        lock.perform(() -> this.ranks.add(rank));
    }

    public void removeRank(Rank rank) {
        lock.perform(() -> this.ranks.remove(rank));
    }

    public void removeRank(String name) {
        lock.perform(() -> this.ranks.removeIf(r -> r.getName().equals(name)));
    }

    public boolean rankExists(String name) {
        return lock.perform(() -> {
            for (Rank rank : this.ranks) {
                if (rank.getName().equals(name))
                    return true;
            }
            return false;
        });
    }

    public void setRank(Player player, int rankIndex) {
        PRankPlayer pRankPlayer = getPlayer(player.getUniqueId());
        int current = pRankPlayer.getRankIndex();
        if(current < rankIndex) {
            pRankPlayer.setRankIndex(rankIndex);
            return;
        }

        for(int i = current+1; i <= rankIndex; i++) {
            Rank rank = getRank(i);
            rank.getCmds().forEach((c) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", player.getName())));
        }
        pRankPlayer.setRankIndex(rankIndex);
    }

    //Player related
    public PRankPlayer getPlayer(UUID player) {
        return lock.perform(() -> {
            PRankPlayer r = this.playerRanks.get(player);
            return r != null ? r : loadPlayer(player);
        });
    }

    public PRankPlayer loadPlayer(UUID player) {
        return lock.perform(() -> {
            try {
                PRankPlayer r = this.playerRanks.get(player);
                if (r != null)
                    return r;


                PRankPlayer pRankPlayer = new PRankPlayer(player, 0, 0);

                if (getPlayerFile(player).exists()) {
                    GravSerializer serializer = new GravSerializer(new FileInputStream(getPlayerFile(player)));

                    try {
                        pRankPlayer = serializer.readObject(player);
                    } catch (Exception ignored) {
                    }
                } else {
                    Rank def = getRank(0);
                    if (Bukkit.getPlayer(player) != null)
                        def.getCmds().forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", Bukkit.getPlayer(player).getName())));
                }

                Rank ra = getRank(pRankPlayer.getRankIndex());

                if (ra == null && this.ranks.size() > 0) {
                    pRankPlayer.setRankIndex(0);
                }

                this.playerRanks.put(player, pRankPlayer);
                return pRankPlayer;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void savePlayer(UUID player) {
        lock.perform(() -> {
            try {
                GravSerializer serializer = new GravSerializer();
                PRankPlayer pRankPlayer = getPlayer(player);
                serializer.writeObject(pRankPlayer);

                serializer.writeToStream(new FileOutputStream(getPlayerFile(player)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void unloadPlayer(UUID player) {
        lock.perform(() -> this.playerRanks.remove(player));
    }

    public File getPlayerFile(UUID player) {
        return new File(parent.getDataFolder() + File.separator + "players", player.toString() + ".pd");
    }
}
