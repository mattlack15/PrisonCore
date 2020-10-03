package com.soraxus.prisons.bunkers.base.elements.storage;

import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

@Getter
@AllArgsConstructor
public class Storage implements GravSerializable {
    private final BunkerResource resource;
    private double amount;
    @Setter
    private double cap;

    public synchronized void addAmount(double am) {
        incrementAmount(am);
    }

    public synchronized void addCap(double am) {
        cap += am;
    }

    public synchronized void addStorage(Storage storage) {
        if (storage.getResource() != resource) {
            throw new IllegalArgumentException("WRONG");
        }
        cap += storage.getCap();
        amount += storage.getAmount();
    }

    public synchronized boolean isFull() {
        return this.amount >= this.cap;
    }

    public Storage(GravSerializer serializer) {
        this.resource = (BunkerResource) serializer.readObject();
        this.amount = serializer.readDouble();
        this.cap = serializer.readDouble();
    }

    public synchronized void setAmount(double amount) {
        this.amount = amount;
    }

    public synchronized void decrementAmount(double amount) {
        this.amount = Math.max(0, amount);
    }
    public synchronized double incrementAmount(double amount) {
        double toAdd = Math.min(this.getCap() - this.getAmount(), amount);
        this.amount += toAdd;
        return amount - toAdd;
    }


    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(resource);
        serializer.writeDouble(amount);
        serializer.writeDouble(cap);
    }

    @Override
    public String toString() {
        return resource.getColor() + resource.getDisplayName() + ": &f" + this.getAmount();
    }
}
