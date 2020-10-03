package com.soraxus.prisons.bunkers.npc.combat.healer;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.NPCAvailableTarget;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.bunkers.npc.effect.EffectType;
import com.soraxus.prisons.bunkers.npc.effect.NPCEffect;
import org.bukkit.entity.Entity;

public class AbilityRegenerateGroup extends BunkerNPCAbility {
    public AbilityRegenerateGroup(AbstractBunkerNPCController parent) {
        super("Regen (group)", parent);
    }

    @Override
    public String getDescription() {
        return "Adds regeneration effect to targeted warrior and surrounding friendlies";
    }

    @Override
    public void use() {
        AvailableTarget<?> target = getParent().getCurrentTarget().getTarget();
        if (!(target instanceof NPCAvailableTarget))
            return;

        NPCAvailableTarget npcTarget = (NPCAvailableTarget) target;
        npcTarget.get().getEffectManager().addEffect(new NPCEffect(EffectType.REGEN, getParent().getParent().getLevel(), 20));

        Entity entity = npcTarget.get().getController().getNpc().getEntity();
        for(Entity surrounding : entity.getNearbyEntities(5, 5, 5)) {
            BunkerNPC npc = getParent().getManager().getNpc(surrounding);
            if(npc != null) {
                npc.getEffectManager().addEffect(new NPCEffect(EffectType.REGEN, getParent().getParent().getLevel(), 20));
            }
        }
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public int cooldownTicks() {
        return 10;
    }
}
