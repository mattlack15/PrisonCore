package com.soraxus.prisons.bunkers.npc;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class NPCManager {
    private ReentrantLock lock = new ReentrantLock(true);
    private List<BunkerNPC> npcList = new ArrayList<>();

    public List<BunkerNPC> getNpcList() {
        lock.lock();
        try {
            return new ArrayList<>(npcList);
        } finally {
            lock.unlock();
        }
    }

    public void addNPC(BunkerNPC npc) {
        lock.lock();
        try {
            if (!this.npcList.contains(npc))
                this.npcList.add(npc);
        } finally {
            lock.unlock();
        }
    }

    public void removeNPC(UUID npcId) {
        lock.lock();
        try {
            this.npcList.removeIf(n -> n.getController() != null && n.getController().isSpawned() && n.getController().getID().equals(npcId));
        } finally {
            lock.unlock();
        }
    }

    public void destroyAllNPCs() {
        this.getNpcList().forEach(n -> {
            try {
                if (n.getController() != null)
                    n.getController().remove();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void tick() {
        getNpcList().forEach(n -> {
            if (n.getController() != null && n.getController().isSpawned()) {
                n.tick();
                n.getController().tick();
            }
        });
    }

    public BunkerNPC getNpc(Entity entity) {
        for (BunkerNPC bunkerNPC : getNpcList()) {
            if (!bunkerNPC.getController().isSpawned()) {
                continue;
            }
            if (bunkerNPC.getController().getNpc().getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return bunkerNPC;
            }
        }
        return null;
    }
}
