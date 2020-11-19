package com.soraxus.prisons.bunkers.npc.combat.healer;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.NPCAvailableTarget;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collection;

public class AbilityHealGroup extends BunkerNPCAbility {

    public AbilityHealGroup(AbstractBunkerNPCController parent) {
        super("Heal (group)", parent);
    }

    @Override
    public String getDescription() {
        return "Heals the target warrior and surrounding friendlies";
    }

    @Override
    public void use() {
        NPCAvailableTarget target = (NPCAvailableTarget) getParent().getCurrentTarget().getTarget();
        Location loc = target.getImmediateLocation();
        World w = loc.getWorld();
        Collection<Entity> entities = w.getNearbyEntities(loc, 5, 5, 5);
        entities.stream().map(ent -> getParent().getManager().getNpc(ent))
                .filter(npc -> npc.getController().getBunker().equals(this.getParent().getBunker()))
                .forEach(npc -> npc.heal(5));
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }
}
