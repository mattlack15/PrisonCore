package com.soraxus.prisons.gangs;

import com.soraxus.prisons.gangs.util.RelationKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

public class GangRelationsManager {

    public static GangRelationsManager instance;

    private Map<RelationKey, GangRelation> relations = new ConcurrentSkipListMap<>((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getGang1().toString(), o2.getGang1().toString()));

    private File file;
    private FileConfiguration config;

    private ReentrantLock ioLock = new ReentrantLock(true);

    private volatile boolean dirty = false;

    public GangRelationsManager(File file) {
        instance = this;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void load() {
        Map<RelationKey, GangRelation> relations = new ConcurrentSkipListMap<>((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getGang1().toString(), o2.getGang1().toString()));
        ioLock.lock();
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            UUID gang1 = UUID.fromString(section.getString("gang1"));
            UUID gang2 = UUID.fromString(section.getString("gang2"));
            RelationKey relationKey = new RelationKey(gang1, gang2);
            GangRelation relation = GangRelation.values()[section.getInt("relation")];
            relations.put(relationKey, relation);
        }
        ioLock.unlock();
        synchronized (this) {
            this.relations = relations;
        }
    }

    public void setRelation(UUID gang1, UUID gang2, GangRelation relation) {
        RelationKey key = new RelationKey(gang1, gang2);
        synchronized (this) {
            relations.put(key, relation);
        }
        ioLock.lock();
        String sectionKey = gang1.toString() + gang2.toString();
        config.set(sectionKey + ".gang1", gang1.toString());
        config.set(sectionKey + ".gang2", gang2.toString());
        config.set(sectionKey + ".relation", relation.ordinal());

        //Get rid of possible alternate section
        config.set(gang2.toString() + gang1.toString(), null);

        markDirty();
        ioLock.unlock();
    }

    public GangRelation getRelation(UUID gang1, UUID gang2) {
        RelationKey key = new RelationKey(gang1, gang2);
        GangRelation result;
        synchronized (this) {
            result = relations.get(key);
        }
        return result;
    }

    private void markDirty() {
        ioLock.lock();
        if (dirty) {
            ioLock.unlock();
            return;
        }
        dirty = true;
        new Thread(() -> {
            try {
                Thread.sleep(50);
                save();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        ioLock.unlock();
    }

    protected void save() {
        ioLock.lock();
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ioLock.unlock();
    }
}
