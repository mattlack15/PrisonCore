package com.soraxus.prisons.gangs;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

public class GangMemberManager {
    public static GangMemberManager instance;

    private File folder;
    private Map<String, GangMember> members = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    private ReentrantLock accessorLock = new ReentrantLock(true);
    private Map<UUID, ReentrantLock> ioLocks = new HashMap<>();
    private ReentrantLock mapLock = new ReentrantLock(true);
    private List<UUID> saving = new ArrayList<>();
    private ReentrantLock savingLock = new ReentrantLock(true);

    public GangMemberManager(File folder) {
        instance = this;
        folder.mkdirs();
        this.folder = folder;
    }

    public File getFile(UUID id) {
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

    private ReentrantLock getIOLock(UUID id) {
        mapLock.lock();
        ReentrantLock lock = ioLocks.get(id);
        if (lock == null) {
            lock = new ReentrantLock(true);
            ioLocks.put(id, lock);
        }
        mapLock.unlock();
        return lock;
    }

    public GangMember load(UUID member) {
        return load(member, true);
    }

    public GangMember load(UUID member, boolean loadBunker) {
        getIOLock(member).lock();
        GangMember member1;
        try {
            File file = getFile(member);
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            member1 = GangMember.fromSection(configuration, this);
        } catch (Exception e) {
            return null;
        } finally {
            getIOLock(member).unlock();
        }
        accessorLock.lock();
        this.members.put(member1.getMember().toString(), member1);
        accessorLock.unlock();

        Gang gang = GangManager.instance.loadGang(member1.getGang(), loadBunker);
        if(gang == null) {
            member1.setGang(null);
        }

        return member1;
    }

    public void unloadMember(UUID member) {
        accessorLock.lock();
        try {
            this.members.remove(member.toString());
        } finally {
            accessorLock.unlock();
        }
        mapLock.lock();
        try {
            ioLocks.remove(member);
        } finally {
            mapLock.unlock();
        }
    }

    public GangMember getMember(OfflinePlayer player) {
        return getMember(player.getUniqueId());
    }

    public GangMember getMember(UUID id) {
        try {
            accessorLock.lock();
            return members.get(id.toString());
        } finally {
            accessorLock.unlock();
        }
    }

    public GangMember getOrMakeMember(OfflinePlayer op) {
        return getOrMakeMember(op.getUniqueId(), op.getName());
    }

    public GangMember getOrMakeMember(UUID id, String username) {
        if (id == null)
            return null;
        GangMember member = getMember(id);
        if (member == null) {
            member = load(id);
            if (member == null) {
                member = new GangMember(this, null, username, id, GangRole.RECRUIT);
                accessorLock.lock();
                members.put(id.toString(), member);
                accessorLock.unlock();
            }
        }
        if (member.getGang() != null) {
            GangManager.instance.loadGang(member.getGang()); //Make sure it's loaded and the bunker is also loaded
        }
        return member;
    }

    public GangMember getOrLoadMember(UUID id) {
        return getOrLoadMember(id, true);
    }

    public GangMember getOrLoadMember(UUID id, boolean loadBunker) {
        try {
            accessorLock.lock();
            GangMember member = getMember(id);
            if (member == null) {
                return load(id, loadBunker);
            }
            return member;
        } finally {
            accessorLock.unlock();
        }
    }

    public Collection<GangMember> getOnlineMembers() {
        try {
            accessorLock.lock();
            return this.members.values();
        } finally {
            accessorLock.unlock();
        }
    }

    public void saveMember(GangMember member) {
        savingLock.lock();
        try {
            if (saving.contains(member.getMember()))
                return;
            saving.add(member.getMember());
        } finally {
            savingLock.unlock();
        }
        ReentrantLock ioLock = getIOLock(member.getMember());
        File file = getFile(member.getMember());
        new Thread(() -> {
            try {
                Thread.sleep(50);
                savingLock.lock();
                saving.remove(member.getMember());
                savingLock.unlock();
                ioLock.lock();
                try {
                    FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    member.toSection(configuration);
                    configuration.save(file);
                } finally {
                    ioLock.unlock();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
