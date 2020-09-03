package com.soraxus.prisons.bunkers.npc.combat.archer;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.Tile;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.string.TextUtil;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

import static com.soraxus.prisons.bunkers.ModuleBunkers.WORLD_PREFIX;

public class AbilityShoot extends BunkerNPCAbility {
    private final NPCArcher npcArcher;

    public AbilityShoot(String name, NPCArcher parent) {
        super(name, parent);
        this.npcArcher = parent;
        EventSubscriptions.instance.subscribe(this);
    }

    @Override
    public String getDescription() {
        return "Shoot an arrow at the target";
    }

    @Override
    public void use() {
        LivingEntity ent = (LivingEntity) npcArcher.getNpc().getEntity();
        Location loc = ent.getEyeLocation();
        Location target = npcArcher.getCurrentTarget().getTarget().getImmediateLocation().clone();
        Arrow arrow = loc.getWorld().spawnArrow(loc.add(loc.getDirection()), target.subtract(loc).toVector(), 1, 0);
        arrow.setShooter(ent);
    }

    @Override
    public boolean canUse() {
        return npcArcher.getCurrentTarget() != null && npcArcher.getCurrentTarget().conditionsMet(npcArcher.getNpc());
    }


    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 12;
    }

    @EventSubscription
    public void onProjectileHit(ProjectileHitEvent e) {
        try {
            ProjectileSource source = e.getEntity().getShooter();
            if (source == getParent().getNpc().getEntity()) {
                Block block = e.getHitBlock();
                if (block == null) {
                    // Hit an entity, don't know what to do
                } else {
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
                        ElementDrop drop = element.getDropForDamage(damage);
                        if (drop != null) {
                            drop.apply(bunker.getAttackingMatch().getAttacker());
                        }
                        element.damage(damage);
                    }
                }
            }
        } catch(Exception ex) {
            ModuleBunkers.messageDevs("Shit went wrong... " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            e.getEntity().remove();
        }
    }
}
