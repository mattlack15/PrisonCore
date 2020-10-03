package com.soraxus.prisons.gangs;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.gangs.events.GangUnloadEvent;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.PrivateMineManager;
import com.soraxus.prisons.util.list.ElementableList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.w3c.dom.Element;

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
    private Map<UUID, ReentrantLock> fileLocks = new HashMap<>();

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
    private ReentrantLock mapLock = new ReentrantLock(true);
    private List<UUID> saving = new ArrayList<>();
    private ReentrantLock savingLock = new ReentrantLock(true);
    private boolean indexBool = false;
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

    private ReentrantLock getIoLock(UUID id) {
        mapLock.lock();
        ReentrantLock lock = fileLocks.get(id);
        if (lock == null) {
            lock = new ReentrantLock(true);
            fileLocks.put(id, lock);
        }
        mapLock.unlock();
        return lock;
    }

    public Gang loadGang(UUID id) {
        if (id == null)
            return null;

        if (this.getLoadedGang(id) != null)
            return getLoadedGang(id);
        ReentrantLock ioLock = getIoLock(id);
        if (!ioLock.tryLock()) {
            return null;
        }
        File file = getFile(id);
        if (!file.exists())
            return null;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Gang gang = Gang.fromSection(config, this, GangMemberManager.instance);
        ioLock.unlock();

        synchronized (this) {
            this.gangs.removeIf(g -> g.getId().equals(gang.getId()));
            this.gangs.add(gang);
        }
        BunkerManager.instance.loadBunkerAsync(gang.getId());
        return gang;
    }

    private File getFile(UUID id) {
        File file = new File(folder, id.toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
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
        String id = this.indexConfig.getString(gangName);
        if (id == null)
            return null;
        return UUID.fromString(id);
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
        ReentrantLock ioLock = getIoLock(gangId);
        ioLock.lock();
        getFile(gangId).delete();
        ioLock.unlock();
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

    public void saveGang(Gang gang) {
        savingLock.lock();
        if (gang == null || saving.contains(gang.getId())) {
            savingLock.unlock();
            return;
        }
        saving.add(gang.getId());
        savingLock.unlock();
        ReentrantLock ioLock = getIoLock(gang.getId());
        File file = getFile(gang.getId());
        new Thread(() -> {
            try {
                Thread.sleep(50);
                savingLock.lock();
                saving.remove(gang.getId());
                savingLock.unlock();
                ioLock.lock();
                try {
                    //Save
                    FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    gang.toSection(configuration);
                    configuration.save(file);
                } finally {
                    ioLock.unlock();
                }

                setGangNameInIndex(gang.getName(), gang.getId());
                saveIndex();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected void setGangNameInIndex(String name, UUID gang) {
        indexLock.lock();
        indexConfig.set(name, gang != null ? gang.toString() : null);
        indexLock.unlock();
    }

    public Gang getOrLoadGang(UUID gangId) {
        Gang g = getLoadedGang(gangId);
        if (g == null) {
            g = loadGang(gangId);
        }
        return g;
    }
}
