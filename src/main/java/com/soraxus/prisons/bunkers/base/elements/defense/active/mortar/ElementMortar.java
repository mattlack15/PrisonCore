package com.soraxus.prisons.bunkers.base.elements.defense.active.mortar;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.defense.active.ActiveDefenseElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.Vector3D;
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
        final double gr = 0.03999999910593033D;
        final double fr = 0.9800000190734863D;

        Vector3D st = Vector3D.fromBukkitVector(start.toVector());
        Vector3D tg = Vector3D.fromBukkitVector(target.toVector());

        Vector3D fst = st.setY(tg.getY());

        double dx = fst.distance(tg);

        /*
        vx(t)=vx_0 * fr^t

		dx = sum(0, t) { vx(t) }
		dx = vx_0 * (1-fr^t)/(1-fr)

		vx_0 = dx * (1-fr)/(1-fr^t)

		vy_0 = v_0 sin(ang)
		vx_0 = v_0 cos(ang)

		vy_0 = dx * ((1-fr)/(1-fr^t)) * tan(ang)

		y_0 is defined as the height the starting point is above the end point

		vy(t) = vy(t-1) * fr - gr

		vy(t) = vy_0 * fr^t - sumGeo(gr, fr, t)

        dy = vy_0 * fr^t * t - sumSumGeo(gr, fr, t)
        vy_0 = (dy + gr * (n - fr*((1-fr^t)/(1-fr))) / (1 - fr)) / (fr^t * t)

        (dy + gr * (n - fr*((1-fr^t)/(1-fr))) / (1 - fr)) = dx * (fr^t * t) * ((1-fr)/(1-fr^t)) * tan(ang)

        dy = a
        gr = b
        fr = c
        dx = d
        ang = e


        g = tan(e)
        h = 1 - c

        k = c^t


        a + b * (n - c*((1-k)/h)) / h = d * g * (k * ln(k)/ln(c)) * (h/(1-k))


         */

        return null;
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
        EventSubscriptions.instance.unSubscribeAll(this);
    }

    @EventSubscription
    private void onExplode(EntityExplodeEvent entityExplodeEvent) {
        if (entityExplodeEvent.getEntity().equals(this.tnt))
            entityExplodeEvent.setCancelled(true);
    }
}
