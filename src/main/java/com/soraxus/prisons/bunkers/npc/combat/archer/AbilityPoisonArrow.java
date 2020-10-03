package com.soraxus.prisons.bunkers.npc.combat.archer;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.NPCAvailableTarget;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.bunkers.npc.effect.EffectType;
import com.soraxus.prisons.bunkers.npc.effect.NPCEffect;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class AbilityPoisonArrow extends BunkerNPCAbility {
    private static final double DAMAGE_MULTIPLIER = 1;

    private static final Vector DIFF = new Vector(0, 1, 0);

    public AbilityPoisonArrow(AbstractBunkerNPCController parent) {
        super("Poison Arrow", parent);
    }

    @Override
    public String getDescription() {
        return "Poison arrow";
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
                    Entity ent = event.getEntity();
                    if (ent != null) {
                        BunkerNPC npc = getParent().getManager().getNpc(ent);
                        npc.getEffectManager().addEffect(
                                new NPCEffect(EffectType.POISON, 1, 10000)
                        );
                    }
                }
            }
        };
        EventSubscriptions.instance.subscribe(metadataValue);
        arrow.setMetadata("spc::event", metadataValue);
        arrow.setShooter(ent);
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 360;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (getParent().getCurrentTarget().getTarget() instanceof NPCAvailableTarget);
    }
}
