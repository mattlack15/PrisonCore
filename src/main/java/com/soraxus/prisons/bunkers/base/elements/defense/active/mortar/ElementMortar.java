package com.soraxus.prisons.bunkers.base.elements.defense.active.mortar;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.defense.active.ActiveDefenseElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class ElementMortar extends ActiveDefenseElement {
    private final Random random = new Random(this.getId().getLeastSignificantBits());
    private Entity tnt = null;
    private Location target = null;

    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementMortar(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    public ElementMortar(Bunker bunker) {
        this(null, bunker);
    }

    private static Vector calculateVelocity(Location start, Location target, double ang) {
        double dy = 0.03999999910593033D;

        double fdd = 0.9800000190734863D;

        Vector3D stv = Vector3D.fromBukkitVector(start.toVector());
        Vector3D tv = Vector3D.fromBukkitVector(target.toVector());
        Vector3D gstv = stv.setY(tv.getY());

        Vector3D dd = tv.subtract(gstv);
        double ax;
        if (dd.getZ() == 0) {
            ax = Math.PI / 2;
        } else {
            ax = Math.atan(dd.getX() / dd.getZ());
        }
        if (dd.getZ() < 0) {
            ax += Math.PI;
        }

        double h = stv.subtract(gstv).length();
        double d = dd.length();
        Bukkit.broadcastMessage("h: " + h + ", d: " + d);

        double lfdd = Math.log(fdd);

        double fdd1r = fdd / (fdd - 1);

        double k = h - d * Math.tan(ang);
        k *= lfdd / dy;
        k /= fdd1r;

        double wi = Math.pow(fdd, k / Math.log(fdd));
        double w = MathUtils.lw(wi);

        double t = (k - w) / lfdd;

        Bukkit.broadcastMessage("t: " + t);

        double fddpt = Math.pow(fdd, t);

        double vy = (h * lfdd - dy * fdd1r * (1 - fddpt - t * lfdd)) / (fddpt - 1);
        double vd = d * lfdd / (fddpt - 1);
        double vx = vd * Math.sin(ax);
        double vz = vd * Math.cos(ax);
        Bukkit.broadcastMessage("v: (" + vx + ", " + vy + ", " + vz + ")");

        return new Vector(vx, -vy, vz);
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public double getMaxHealth() {
        return 250 * getLevel();
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.DEFENSIVE_MORTAR;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    @Override
    protected void onDefendingMatchEnd() {
        if (tnt != null) {
            if (!tnt.isDead()) {
                tnt.remove();
            }
            tnt = null;
        }
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!this.isDefendingMatchActive())
            return;

        Match match = getBunker().getDefendingMatch();

        if (tnt == null || tnt.isDead()) {
            //Pick new target
            List<AvailableTarget<?>> targetList = match.getDefenderTargets();
            if (targetList.isEmpty())
                return;
            AvailableTarget<?> picked = targetList.get(random.nextInt(targetList.size()));
            target = picked.getImmediateLocation();

            //Fire at that location
            tnt = getBunker().getWorld().getBukkitWorld().spawnEntity(this.getSpawnLocation(), EntityType.PRIMED_TNT);

            tnt.setVelocity(calculateVelocity(this.getSpawnLocation(), target, Math.PI / 6D));
        }
    }

    private Location getSpawnLocation() {
        return getLocation().clone().add(0, 5, 0);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        EventSubscriptions.instance.subscribe(this);
    }

    @Override
    protected void onDisable() {
        EventSubscriptions.instance.unSubscribe(this);
    }

    @EventSubscription
    private void onExplode(EntityExplodeEvent entityExplodeEvent) {
        if (entityExplodeEvent.getEntity().equals(this.tnt))
            entityExplodeEvent.setCancelled(true);
    }
}
