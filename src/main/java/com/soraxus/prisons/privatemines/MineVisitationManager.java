package com.soraxus.prisons.privatemines;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.locks.CustomLock;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MineVisitationManager implements GravSerializable {
    private final PrivateMine parent;

    private final AtomicLong rentalPrice = new AtomicLong();

    private final CustomLock visitorLock = new CustomLock(true);
    private final List<MineVisitor> visitors = new ArrayList<>();
    @Getter
    private final AtomicInteger atomicRentedSlots = new AtomicInteger();

    public MineVisitationManager(PrivateMine parent) {
        this.parent = parent;
        EventSubscriptions.instance.subscribe(this);
    }

    /**
     * Deserialize a MineVisitationManager from a GravSerializer, and a parent Mine
     *
     * @param serializer The serializer
     * @param parent     The parent Mine
     * @return The deserialized MineVisitationManager
     */
    public static MineVisitationManager deserialize(GravSerializer serializer, PrivateMine parent) {
        MineVisitationManager manager = new MineVisitationManager(parent);
        manager.rentalPrice.set(serializer.readLong());
        manager.atomicRentedSlots.set(serializer.readInt());
        return manager;
    }

    /**
     * Get a list of the currently allowed visitors (They will almost definitely be currently inside the mine) <br></br>
     * Visitors are removed when they leave the mine
     */
    public List<MineVisitor> getVisitors() {
        return visitorLock.perform(() -> new ArrayList<>(this.visitors));
    }

    public List<MineVisitor> getVisitorsOfType(VisitationType type) {
        return visitorLock.perform(() -> this.visitors.stream()
                .filter(v -> v.getVisitationType().equals(type))
                .collect(Collectors.toList()));
    }

    /**
     * Get the visitor associated with the given UUID
     *
     * @param visitor The UUID of the visitor
     * @return The visitor associated with the given UUID, or null if none is found
     */
    public MineVisitor getVisitor(UUID visitor) {
        return visitorLock.perform(() -> {
            for (MineVisitor mineVisitor : visitors)
                if (mineVisitor.getVisitor().equals(visitor))
                    return mineVisitor;
            return null;
        });
    }

    public VisitationType tryAddVisitor(UUID visitor) {
        if (!this.parent.getGang().isMember(visitor)) {
            if (this.addVisitor(visitor, VisitationType.RENTAL)) {
                return VisitationType.RENTAL;
            }
        } else if (this.addVisitor(visitor, VisitationType.FREE)) {
            return VisitationType.FREE;
        }
        return null;
    }

    /**
     * Add a visitor to the visitor list
     *
     * @param visitor The UUID of the visitor
     * @return True if the visitor was added, false if there is no more slots for this visitor's type of visitation
     */
    public boolean addVisitor(UUID visitor, VisitationType type) {
        return visitorLock.perform(() -> {
            if (getVisitor(visitor) != null) {
                removeVisitor(visitor);
            }
            if (type.equals(VisitationType.FREE) && getAvailableNonRentedSlots() == 0)
                return false;
            if (type.equals(VisitationType.RENTAL) && getAvailableRentedSlots() == 0)
                return false;
            MineVisitor visitor1 = new MineVisitor(visitor);
            visitor1.setVisitationType(type);
            this.visitors.add(visitor1);
            return true;
        });
    }

    /**
     * Remove a visitor from the visitor list
     *
     * @param visitor The UUID of the visitor
     */
    public void removeVisitor(UUID visitor) {
        this.visitorLock.perform(() -> this.visitors.removeIf(v -> v.getVisitor().equals(visitor)));
    }

    /**
     * Get the rental price
     *
     * @return the price
     */
    public long getRentalPrice() {
        return this.rentalPrice.get();
    }

    /**
     * Set the rental price
     *
     * @param price the price
     */
    public void setRentalPrice(long price) {
        long oldPrice = this.rentalPrice.getAndSet(price);
        this.visitorLock.perform(() -> {
            for (MineVisitor visitor : visitors) {
                if (visitor.getVisitationType().equals(VisitationType.FREE))
                    continue;

                Player player = Bukkit.getPlayer(visitor.getVisitor());
                AtomicInteger session = visitor.getCurrentSessionTicks();
                int sessionTicks = session.get();
                Economy moneyEco = Economy.money;
                long p = (long) (oldPrice * (sessionTicks / 60D));
                if (!moneyEco.hasBalance(visitor.getVisitor(), p)) {
                    // Not enough money, kick out
                    player.sendMessage("§cYou are no longer able to pay the rent for this private mine!");
                    player.teleport(SpigotPrisonCore.instance.getSpawn());
                    // Don't set session time, so that next time they try to teleport it instantly takes money
                } else {
                    // Pay rent
                    moneyEco.removeBalance(visitor.getVisitor(), p);
                    parent.getGang().addBalance(p);
                    session.set(0);
                    player.sendMessage("§cThis Gang Mine's rent has changed to " + price + " per minute.");
                    player.sendMessage("§cIf this price is more than you are willing to pay, please leave within 60 seconds.");
                }
            }
        });
    }

    /**
     * Get the rental price as an atomic long
     *
     * @return the price as an atomic long
     */
    public AtomicLong getAtomicRentalPrice() {
        return this.rentalPrice;
    }

    public int getSlots() {
        return this.parent.getGang().getLevelInt();
    }

    public int getNonRentedSlots() {
        return this.getSlots() - this.getAtomicRentedSlots().get();
    }

    public int getAvailableRentedSlots() {
        return this.getAtomicRentedSlots().get() - getVisitorsOfType(VisitationType.RENTAL).size();
    }

    public int getAvailableNonRentedSlots() {
        return this.getNonRentedSlots() - getVisitorsOfType(VisitationType.FREE).size();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeLong(rentalPrice.get());
        serializer.writeInt(atomicRentedSlots.get());
    }

    //Technically redundant because visitors are teleported out of the mine when they quit
    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        this.removeVisitor(event.getPlayer().getUniqueId());
    }

    @EventSubscription(priority = EventPriority.MONITOR) //Monitor so it can't (probably won't) be cancelled afterwards
    private void onTeleport(PlayerTeleportEvent event) {
        if (parent.getWorld() == null || event.isCancelled())
            return;
        if (event.getFrom().getWorld().equals(this.parent.getWorld()) && !event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            this.removeVisitor(event.getPlayer().getUniqueId()); //Remove from the visitor list
        }
    }
}
