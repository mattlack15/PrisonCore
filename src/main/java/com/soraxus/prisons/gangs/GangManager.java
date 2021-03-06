package com.soraxus.prisons.gangs;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.errors.ModuleErrors;
import com.soraxus.prisons.gangs.events.GangUnloadEvent;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.PrivateMineManager;
import com.soraxus.prisons.util.list.ElementableList;
import com.soraxus.prisons.util.list.LockingList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class GangManager {
    public static GangManager instance;
    private ElementableList<Gang> gangs = new ElementableList<>();
    private File folder;
    private File indexFile;
    private FileConfiguration indexConfig;
    private ReentrantLock indexLock = new ReentrantLock(true);

    //    public void load(GangMemberManager memberManager) {
//        List<Gang> gangs = new ArrayList<>();
//
//        //Load Gangs
//        ioLock.lock();
//        for(String key : config.getKeys(false)) {
//            Gang gang = Gang.fromSection(config.getConfigurationSection(key), this);
//            gangs.add(gang);
//        }
//        ioLock.unlock();
//
//        //Refresh Members
//        Map<String, List<GangMember>> members = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
//        for(GangMember member : memberManager.getAllMembers()) {
//            UUID gangId = member.getGang();
//            if(gangId != null) {
//                List<GangMember> list = members.computeIfAbsent(gangId.toString(), k -> new ArrayList<>());
//                list.add(member);
//            }
//        }
//        gangs.forEach(g -> {
//            List<GangMember> list = members.getOrDefault(g.getId().toString(), new ArrayList<>());
//            g.setCachedMembers(list);
//        });
//
//        //Set Reference
//        synchronized (this) {
//            this.gangs = gangs;
//        }
//    }
    private volatile boolean indexBool = false;
    private ReentrantLock indexLockBool = new ReentrantLock(true);

    public GangManager(File folder) {
        instance = this;
        folder.mkdirs();
        this.folder = folder;
        indexFile = new File(folder, "gang-index.yml");
        if (!indexFile.exists()) {
            try {
                indexFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        indexConfig = YamlConfiguration.loadConfiguration(indexFile);
    }

    public synchronized Gang getLoadedGang(UUID id) {
        for (Gang gang : gangs)
            if (gang.getId().equals(id))
                return gang;
        return null;
    }

    public synchronized Gang getLoadedGang(String name) {
        for (Gang gang : gangs)
            if (gang.getName().equalsIgnoreCase(name))
                return gang;
        return null;
    }

    public Gang loadGang(UUID id) {
        return loadGang(id, true);
    }

    public Gang loadGang(UUID id, boolean loadBunker) {
        if (id == null)
            return null;

        if (this.getLoadedGang(id) != null) {
            if (BunkerManager.instance.getLoadedBunker(id) == null && loadBunker)
                BunkerManager.instance.loadBunkerAsync(id);
            return getLoadedGang(id);
        }
        saveLock.lock();
        File file = getFile(id);
        if (!file.exists()) {
            saveLock.unlock();
            return null;
        }
        Gang gang;
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            gang = Gang.fromSection(config, this, GangMemberManager.instance);
        } finally {
            saveLock.unlock();
        }

        synchronized (this) {
            this.gangs.removeIf(g -> g.getId().equals(gang.getId()));
            this.gangs.add(gang);
        }

        if (loadBunker)
            BunkerManager.instance.loadBunkerAsync(gang.getId());

        return gang;
    }

    private File getFile(UUID id) {
        return new File(folder, id.toString() + ".yml");
    }

    /**
     * Returns null if gang with name already exists
     */
    public synchronized Gang createGang(String name) {
        Gang gang = getLoadedGang(name);
        if (gang != null)
            return null;
        gang = new Gang(this, name);
        this.gangs.add(gang);
        saveGang(gang);
        return gang;
    }

    public UUID getId(String gangName) {
        if (gangName == null)
            return null;
        ConfigurationSection section = this.indexConfig.getConfigurationSection(gangName);
        if (section == null)
            return null;
        if (!section.isSet("id") || !section.isString("id")) {
            return null;
        }
        return UUID.fromString(section.getString("id"));
    }

    public boolean gangExists(String name) {
        return getId(name) != null;
    }

    public boolean gangExists(UUID id) {
        return new File(folder, id.toString() + ".yml").exists();
    }

    public List<String> listGangs() {
        return new ArrayList<>(this.indexConfig.getKeys(false));
    }

    public void disbandGang(UUID gangId) {
        synchronized (this) {
            this.gangs.removeIf(g -> {
                if (g.getId().equals(gangId)) {
                    g.onDisband();
                    indexLock.lock();
                    indexConfig.set(g.getName(), null);
                    indexLock.unlock();
                    saveIndex();
                    return true;
                }
                return false;
            });
        }
        saveLock.lock();
        try {
            File file = getFile(gangId);
            file.delete();
        } finally {
            saveLock.unlock();
        }
        BunkerManager.instance.syncDeleteBunker(gangId);
    }

    public void unload(UUID gangId) {
        synchronized (this) {
            Gang g = this.gangs.byFunction(gangId, Gang::getId);
            if (g == null) {
                return;
            }
            GangUnloadEvent event = new GangUnloadEvent(g);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            this.gangs.remove(g);
        }

        //Unload bunker
        BunkerManager.instance.tryUnload(BunkerManager.instance.getLoadedBunker(gangId));

        //Unload private mine
        PrivateMine mine = PrivateMineManager.instance.getLoadedPrivateMine(gangId);
        if (mine != null)
            PrivateMineManager.instance.saveAndUnloadPrivateMineAsync(mine);
    }

    public void saveIndex() {
        indexLockBool.lock();
        if (indexBool) {
            indexLockBool.unlock();
            return;
        }
        indexBool = true;
        indexLockBool.unlock();
        new Thread(() -> {
            try {
                Thread.sleep(50);
                indexLockBool.lock();
                indexBool = false;
                indexLockBool.unlock();
                try {
                    indexLock.lock();
                    try {
                        indexConfig.save(indexFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } finally {
                    indexLock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private final LockingList<Gang> saveQueue = new LockingList<>();
    private final ReentrantLock saveLock = new ReentrantLock(true);

    public void flushSaveQueue() {

        //Copy and clear saveQueue
        List<Gang> save = new ArrayList<>();
        saveQueue.getLock().perform(() -> {
            save.addAll(saveQueue);
            saveQueue.clear();
        });

        saveLock.lock();
        try {
            for (Gang gang : save) {
                try {

                    //Get file
                    File file = getFile(gang.getId());
                    file.createNewFile();

                    //Save to file
                    FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    gang.toSection(configuration);
                    configuration.save(file);

                    cacheGang(gang);
                } catch (Exception e) {
                    ModuleErrors.instance.recordError(e, "Saving gang " + gang.getName(), "stopped saving the gang");
                    e.printStackTrace();
                }
            }

            //Save index
            saveIndex();
        } finally {
            saveLock.unlock();
        }
    }

    /**
     * Queues a save operation of a gang
     */
    public void saveGang(Gang gang) {
        saveQueue.getLock().perform(() -> {
            if (!saveQueue.contains(gang)) {
                saveQueue.add(gang);
            }
        });
    }

    public LockingList<Gang> getSaveQueue() {
        return this.saveQueue;
    }

    public void cacheGang(Gang gang) {
        indexLock.lock();
        ConfigurationSection section = indexConfig.createSection(gang.getName());
        section.set("id", gang.getId().toString());
        section.set("xp", gang.getXp());
        indexLock.unlock();
    }

    public void uncacheGang(String gangName) {
        indexLock.lock();
        indexConfig.set(gangName, null);
        indexLock.unlock();
    }

    public Gang getOrLoadGang(UUID gangId) {
        return getOrLoadGang(gangId, true);
    }

    public Gang getOrLoadGang(UUID gangId, boolean loadBunker) {
        Gang g = getLoadedGang(gangId);
        if (g == null) {
            g = loadGang(gangId, loadBunker);
        }
        return g;
    }

    public Map<UUID, Long> getCachedGangXpMap() {
        indexLock.lock();
        try {
            Map<UUID, Long> map = new HashMap<>();
            for (String key : indexConfig.getKeys(false)) {
                ConfigurationSection section = indexConfig.getConfigurationSection(key);
                UUID id = UUID.fromString(section.getString("id"));
                long xp = section.getLong("xp");
                map.put(id, xp);
            }
            return map;
        } finally {
            indexLock.unlock();
        }
    }
}
