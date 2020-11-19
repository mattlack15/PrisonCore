package com.soraxus.prisons.bunkers.npc.combat.bomber;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.asyncworld.AsyncWorld;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AbilityBlowUp extends BunkerNPCAbility {

    private final List<UUID> fallingBlocks = new ArrayList<>();

    public AbilityBlowUp(AbstractBunkerNPCController parent) {
        super("Bomb", parent);
    }

    @Override
    public String getDescription() {
        return "Blow up the enemy";
    }

    @Override
    public void use() {
        World w = getParent().getWorld();
        AsyncWorld world = new SpigotAsyncWorld(getParent().getWorld());
        List<Location> blockLocations = Collections.synchronizedList(new ArrayList<>());
        Vector3D loc = Vector3D.fromBukkitVector(getParent().getLocation().toBukkitVector());
        CuboidRegion region = new CuboidRegion(getParent().getWorld(), loc.subtract(3, 1, 3), loc.add(3, 3, 3));
        world.asyncForAllInRegion(region, (l, id, tag, lighting) -> {
            if (ThreadLocalRandom.current().nextInt(6) == 0) {
                blockLocations.add(new Location(region.getWorld(), l.getX(), l.getY(), l.getZ()));
            }
        }, true);
        blockLocations.forEach(l -> {
            FallingBlock block = w.spawnFallingBlock(l.add(0, 1, 0), l.getBlock().getType(), l.getBlock().getData());
            block.setDropItem(false);
            block.setInvulnerable(true);
            block.setHurtEntities(false);
            MetadataValue value = new MetadataValue() {
                @Override
                public Object value() {
                    return null;
                }

                @Override
                public int asInt() {
                    return 0;
                }

                @Override
                public float asFloat() {
                    return 0;
                }

                @Override
                public double asDouble() {
                    return 0;
                }

                @Override
                public long asLong() {
                    return 0;
                }

                @Override
                public short asShort() {
                    return 0;
                }

                @Override
                public byte asByte() {
                    return 0;
                }

                @Override
                public boolean asBoolean() {
                    return false;
                }

                @Override
                public String asString() {
                    return null;
                }

                @Override
                public Plugin getOwningPlugin() {
                    return SpigotPrisonCore.instance;
                }

                @Override
                public void invalidate() {
                    EventSubscriptions.instance.unSubscribe(this);
                }

                @EventSubscription
                public void onFall(EntityChangeBlockEvent event) {
                    if (event.getEntity() == block) {
                        event.setCancelled(true);
                        block.remove();
                    }
                }
            };
            EventSubscriptions.instance.subscribe(value);
            block.setMetadata("deplosion", value);
            Vector vel = l.toVector().subtract(getParent().getLocation().subtract(0, 1.2, 0)
                    .toBukkitVector())
                    .normalize().multiply(0.9);
            block.setVelocity(vel);
        });

        //Damage NPCs
        getParent().getNpc().getEntity().getNearbyEntities(3, 3, 3).forEach(entity -> {
            BunkerNPC npc = getParent().getManager().getNpc(entity);
            if (npc != null) {
                AbstractBunkerNPCController controller = npc.getController();
                if (!(controller instanceof CombatNPCController)) {
                    return;
                }
                Bunker defender = ((CombatNPCController) controller).getCurrentMatch().getDefender();
                if (controller.getBunker() == defender) {
                    npc.damage(5 * getParent().getParent().getLevel());
                }
            }
        });

        //Damage elements
        if (((CombatNPCController) getParent()).getCurrentMatch() != null) {
            List<BunkerElement> elements = ((CombatNPCController) getParent()).getCurrentMatch().getDefender().getTileMap().getElements();
            elements.removeIf((e) -> e.getBoundingRegion(5).smallestDistance(loc) > 10);
            elements.forEach(e -> e.damage(getParent().getParent().getLevel() * 15));
        }

        //Explosion effect
        getParent().getWorld().spawnParticle(Particle.EXPLOSION_HUGE,
                getParent().getLocation().toBukkitVector().toLocation(getParent().getWorld()),
                1);

        //Sound effect
        getParent().getWorld().playSound(getParent().getLocation().toBukkitVector().toLocation(getParent().getWorld()),
                Sound.ENTITY_GENERIC_EXPLODE,
                1.2F, 1F);

        //Remove the NPC
        getParent().remove();
    }

    @Override
    public boolean canUse() {
        return getParent().getCurrentTarget() != null && getParent().getCurrentTarget().conditionsMet(getParent().getNpc()) && getParent().getCurrentTarget().exists();
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 1;
    }
}
