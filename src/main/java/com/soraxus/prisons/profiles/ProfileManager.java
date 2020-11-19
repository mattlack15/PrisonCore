package com.soraxus.prisons.profiles;

import com.soraxus.prisons.SpigotPrisonCore;
import net.ultragrav.serializer.Meta;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ProfileManager {

    //Instance
    public static ProfileManager instance;

    //Map
    private final Map<UUID, PrisonProfile> profileMap = new HashMap<>();
    private final ReentrantLock profileLock = new ReentrantLock(true);

    //Updates
    private final ReentrantLock queueLock = new ReentrantLock(true);
    private final List<PrisonProfile> insertQueue = new ArrayList<>();
    private final List<PrisonProfile> updateQueue = new ArrayList<>();

    //Executor
    private final ExecutorService service = Executors.newFixedThreadPool(8);

    //Database
    private String host;
    private String database;
    private int port;
    private String username;
    private String password;

    public ProfileManager(String host, String database, int port, String username, String password) {
        instance = this;
        this.host = host;
        this.database = database;
        this.port = port;
        this.username = username;
        this.password = password;
        try {
            ProfileSQL sql = new ProfileSQL();
            sql.connect(host, database, port, username, password);
            sql.createTables();
            sql.disconnect();
            System.out.println("Connected to profile database!");
            Bukkit.getScheduler().runTaskTimerAsynchronously(SpigotPrisonCore.instance, this::flushUpdateQueue, 0, 10);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public void flushUpdateQueue() {
        List<PrisonProfile> update = new ArrayList<>();
        List<PrisonProfile> insert = new ArrayList<>();

        if (updateQueue.size() == 0 && insertQueue.size() == 0)
            return;

        long ms = System.currentTimeMillis();

        ProfileSQL sql = new ProfileSQL();
        try {

            sql.connect(this.host, this.database, this.port, this.username, this.password);

            this.queueLock.lock();
            try {
                update.addAll(updateQueue);
                insert.addAll(insertQueue);
                updateQueue.clear();
                insertQueue.clear();
            } finally {
                this.queueLock.unlock();
            }

            if (insert.size() != 0)
                sql.insertProfiles(insert);
            if (update.size() != 0)
                sql.updateProfiles(update);

            sql.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sql.disconnect();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
        ms = System.currentTimeMillis() - ms;
    }

    public PrisonProfile getLoadedProfile(UUID playerId) {
        profileLock.lock();
        try {
            return profileMap.get(playerId);
        } finally {
            profileLock.unlock();
        }
    }

    public CompletableFuture<PrisonProfile> loadOrCreateProfile(UUID playerId) {

        CompletableFuture<PrisonProfile> future = new CompletableFuture<>();

        PrisonProfile prisonProfile = getLoadedProfile(playerId);
        if (prisonProfile != null) {
            future.complete(prisonProfile);
            return future;
        }

        service.submit(() -> {
            try {
                ProfileSQL sql = new ProfileSQL();
                sql.connect(host, database, port, username, password);
                Meta m = sql.getProfile(playerId);

                boolean created = false;

                if (m == null) {
                    created = true;
                    m = new Meta();
                }

                sql.disconnect();

                PrisonProfile profile;

                profileLock.lock();
                try {
                    profile = this.updateProfile(playerId, m);
                    if (created)
                        this.queueInsert(profile);
                } finally {
                    profileLock.unlock();
                }

                future.complete(profile);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        return future;
    }

    public CompletableFuture<PrisonProfile> loadProfile(UUID playerId) {
        CompletableFuture<PrisonProfile> future = new CompletableFuture<>();

        PrisonProfile prisonProfile = getLoadedProfile(playerId);
        if (prisonProfile != null) {
            future.complete(prisonProfile);
            return future;
        }

        service.submit(() -> {
            try {
                ProfileSQL sql = new ProfileSQL();
                sql.connect(host, database, port, username, password);
                Meta m = sql.getProfile(playerId);
                if (m == null) {
                    future.complete(null);
                    return;
                }
                sql.disconnect();
                PrisonProfile profile = this.updateProfile(playerId, m);
                future.complete(profile);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        return future;
    }

    public void unloadProfile(UUID playerId) {
        profileLock.lock();
        try {
            this.profileMap.remove(playerId);
        } finally {
            profileLock.unlock();
        }
    }

    private PrisonProfile updateProfile(UUID profile, Meta meta) {
        profileLock.lock();
        try {
            PrisonProfile p = getLoadedProfile(profile);
            if (p != null) {
                p.setMeta(meta);
            } else {
                this.profileMap.put(profile, new PrisonProfile(profile, meta));
            }
            return getLoadedProfile(profile);
        } finally {
            profileLock.unlock();
        }
    }

    public void queueUpdate(PrisonProfile profile) {
        queueLock.lock();
        try {
            updateQueue.add(profile);
        } finally {
            queueLock.unlock();
        }
    }

    public void queueInsert(PrisonProfile profile) {
        queueLock.lock();
        try {
            insertQueue.add(profile);
        } finally {
            queueLock.unlock();
        }
    }

}
