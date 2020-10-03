package com.soraxus.prisons.bunkers.npc.combat.healer;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.NPCAvailableTarget;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import com.soraxus.prisons.bunkers.npc.effect.EffectType;
import com.soraxus.prisons.bunkers.npc.effect.NPCEffect;

public class AbilityRegenerateSingle extends BunkerNPCAbility {
    public AbilityRegenerateSingle(AbstractBunkerNPCController parent) {
        super("Regen (single)", parent);
    }

    @Override
    public String getDescription() {
        return "Adds regeneration effect to target friendly warrior";
    }

    @Override
    public void use() {
        AvailableTarget<?> target = getParent().getCurrentTarget().getTarget();
        if (!(target instanceof NPCAvailableTarget))
            return;

        NPCAvailableTarget npcTarget = (NPCAvailableTarget) target;
        npcTarget.get().getEffectManager().addEffect(new NPCEffect(EffectType.REGEN, getParent().getParent().getLevel(), 20));
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
