package com.soraxus.prisons.bunkers.npc.combat.healer;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.NPCAvailableTarget;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class AbilityHealSingle extends BunkerNPCAbility {
    public AbilityHealSingle(AbstractBunkerNPCController parent) {
        super("Heal (single)", parent);
    }

    @Override
    public String getDescription() {
        return "Heals the target warrior";
    }

    @Override
    public void use() {
        LivingEntity ent = (LivingEntity) getParent().getNpc().getEntity();
        NPCAvailableTarget target = (NPCAvailableTarget) getParent().getCurrentTarget().getTarget();
        BunkerNPC npc = target.get();
        npc.heal(5);
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (getParent().getCurrentTarget().getTarget() instanceof NPCAvailableTarget); // TODO: Friendly check
    }
}
