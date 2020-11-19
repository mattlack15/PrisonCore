package com.soraxus.prisons.bunkers.npc.combat.archer;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;

public class AbilityExplosiveArrow extends BunkerNPCAbility {

    private static final double DAMAGE_MULTIPLIER = 1;

    private static final Vector DIFF = new Vector(0, 1, 0);

    public AbilityExplosiveArrow(AbstractBunkerNPCController parent) {
        super("Exploding Arrow", parent);
    }

    @Override
    public String getDescription() {
        return "Exploding arrow";
    }

    @Override
    public void use() {
        Location target = getTargetImmediateLocation();
        LivingEntity ent = (LivingEntity) getParent().getNpc().getEntity();
        Location loc = ent.getEyeLocation();
        Arrow arrow = loc.getWorld().spawnArrow(loc.add(target.subtract(loc).toVector().normalize()), target.subtract(loc).toVector().multiply(1.5D).add(DIFF), 1, 0);
        FixedMetadataValue metadataValue = new FixedMetadataValue(SpigotPrisonCore.instance, null) {
            @Override
            public void invalidate() {
                super.invalidate();
                EventSubscriptions.instance.unSubscribe(this);
            }

            @EventSubscription
            private void onHit(ProjectileHitEvent event) {
                if (event.getEntity().getUniqueId().equals(arrow.getUniqueId())) {
                    event.getEntity().remove();
                    explode(event.getEntity().getLocation());
                }
            }
        };
        EventSubscriptions.instance.subscribe(metadataValue);
        arrow.setMetadata("spc::event", metadataValue);
        arrow.setShooter(ent);
    }

    public void explode(Location location) {

        //Damage NPCs
        location.getWorld().getNearbyEntities(location, 3, 3, 3).forEach(entity -> {
            BunkerNPC npc = getParent().getManager().getNpc(entity);
            if (npc != null) {
                AbstractBunkerNPCController controller = npc.getController();
                if (!(controller instanceof CombatNPCController)) {
                    return;
                }
                Bunker defender = ((CombatNPCController) controller).getCurrentMatch().getDefender();
                if (controller.getBunker() == defender) {
                    npc.damage(4 * getParent().getParent().getLevel() * DAMAGE_MULTIPLIER); //Damage NPCs
                }
            }
        });

        //Damage elements
        if (((CombatNPCController) getParent()).getCurrentMatch() != null) {
            List<BunkerElement> elements = ((CombatNPCController) getParent()).getCurrentMatch().getDefender().getTileMap().getElements();
            elements.removeIf((e) -> e.getBoundingRegion(5).smallestDistance(Vector3D.fromBukkitVector(location.toVector())) > 10);
            elements.forEach(e -> e.damage(getParent().getParent().getLevel() * 15 * DAMAGE_MULTIPLIER)); //Damage elements
        }

        //Explosion effect
        getParent().getWorld().spawnParticle(Particle.EXPLOSION_HUGE,
                getParent().getLocation().toBukkitVector().toLocation(getParent().getWorld()),
                1);

        //Sound effect
        getParent().getWorld().playSound(getParent().getLocation().toBukkitVector().toLocation(getParent().getWorld()),
                Sound.ENTITY_GENERIC_EXPLODE,
                1.2F, 1F);

    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 360;
    }
}
