package com.soraxus.prisons.bunkers.base.elements.storage;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.util.CastUtil;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public abstract class StorageElement extends BunkerElement {
    private final List<Storage> storageList;
    private final ReentrantLock storageLock = new ReentrantLock(true);

    public StorageElement(Bunker bunker, Storage... resources) {
        super(null, bunker);
        storageLock.lock();
        try {
            storageList = Arrays.asList(resources);
            getMeta().set("storageList", storageList);
        } finally {
            storageLock.unlock();
        }
    }

    public StorageElement(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
        storageLock.lock();
        try {
            if (serializer == null) {
                storageList = new ArrayList<>();
                return;
            }
            storageList = CastUtil.cast(getMeta().get("storageList"));
        } finally {
            storageLock.unlock();
        }
    }

    public Storage getStorage(BunkerResource resource) {
        storageLock.lock();
        try {
            for (Storage storage : storageList) {
                if (storage.getResource() == resource) {
                    return storage;
                }
            }
            return null; // Fuck you, I don't have this storage
        } finally {
            storageLock.unlock();
        }
    }

    public double getHolding(BunkerResource resource) {
        storageLock.lock();
        try {
            return getStorage(resource).getAmount();
        } finally {
            storageLock.unlock();
        }
    }

    /**
     * Set the current amount this element is holding
     * Note: this does not perform capacity checks
     *
     * @param amount New value
     */
    public void setHolding(BunkerResource resource, double amount) {
        storageLock.lock();
        try {
            getStorage(resource).setAmount(amount);
        } finally {
            storageLock.unlock();
        }
    }

    /**
     * Add to the current amount this element is holding
     * Note: this does not perform capacity checks
     *
     * @param amount Amount to add
     */
    public void addHolding(BunkerResource resource, double amount) {
        storageLock.lock();
        try {
            setHolding(resource, getHolding(resource) + amount);
        } finally {
            storageLock.unlock();
        }
    }

    public abstract double getCapacity(BunkerResource resource);

    @Override
    protected void onLevelSet(int level) {
        storageLock.lock();
        try {
            for (Storage storage : this.getStorageList()) {
                storage.setCap(getCapacity(storage.getResource()));
            }
        } finally {
            storageLock.unlock();
        }
    }
}
