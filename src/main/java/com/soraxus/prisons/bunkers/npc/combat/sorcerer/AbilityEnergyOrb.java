package com.soraxus.prisons.bunkers.npc.combat.sorcerer;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.string.TextUtil;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

import static com.soraxus.prisons.bunkers.ModuleBunkers.WORLD_PREFIX;

public class AbilityEnergyOrb extends BunkerNPCAbility {
    public AbilityEnergyOrb(AbstractBunkerNPCController parent) {
        super("Energy Orb", parent);
    }

    @Override
    public String getDescription() {
        return "Cast an energy orb and shoot it at the enemy";
    }

    @Override
    public void use() {
        LivingEntity ent = (LivingEntity) getParent().getNpc().getEntity();
        Location loc = ent.getEyeLocation();
        Location target = getParent().getCurrentTarget().getTarget().getImmediateLocation().clone();
        // TODO: What entity looks like an energy orb? Or particles?
        Fireball ball = (Fireball) loc.getWorld().spawnEntity(loc.add(loc.getDirection()), EntityType.FIREBALL);
        ball.setDirection(target.subtract(loc).toVector().multiply(1.5D));
        ball.setShooter(ent);
        ball.setYield(0);
        FixedMetadataValue metadataValue = new FixedMetadataValue(SpigotPrisonCore.instance, null) {
            @Override
            public void invalidate() {
                super.invalidate();
                EventSubscriptions.instance.unSubscribe(this);
            }

            @EventSubscription
            private void onHit(ProjectileHitEvent e) {
                if (e.getEntity() == ball) {
                    Block block = e.getHitBlock();
                    if (block == null) {
                        //Hit an entity
                        BunkerNPC npc = getParent().getManager().getNpc(e.getHitEntity());
                        if (npc != null) {
                            double damage = 3.141592653589 * getParent().getParent().getLevel();
                            npc.damage(damage);
                        }
                    } else {
                        //Hit a block/element
                        Location loc = block.getLocation();
                        String worldName = loc.getWorld().getName();
                        String id = worldName.substring(WORLD_PREFIX.length());
                        id = TextUtil.insertDashUUID(id);
                        Bunker bunker = BunkerManager.instance.getLoadedBunker(UUID.fromString(id));
                        IntVector2D pos = bunker.getWorld().getTileAt(loc);
                        if (bunker.getTileMap().isWithin(pos)) {
                            Tile tile = bunker.getTileMap().getTile(pos);
                            if (tile == null) {
                                return;
                            }
                            BunkerElement element = tile.getParent();
                            if (element == null) {
                                return;
                            }
                            double damage = 3.141592653589 * getParent().getParent().getLevel();
                            ElementDrop drop = element.damage(damage);
                            if (drop != null) {
                                drop.apply(bunker.getDefendingMatch().getAttacker());
                            }
                        }
                    }
                }
            }
        };
        EventSubscriptions.instance.subscribe(metadataValue);
        ball.setMetadata("bunker_fireball_event", metadataValue);
    }

    @Override
    public boolean canUse() {
        return getParent().getCurrentTarget() != null && getParent().getCurrentTarget().exists();
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 100;
    }
}
