package com.soraxus.prisons.bunkers.npc.combat.healer;

import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;

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
