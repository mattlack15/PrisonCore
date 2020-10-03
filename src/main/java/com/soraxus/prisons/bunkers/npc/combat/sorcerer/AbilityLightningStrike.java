package com.soraxus.prisons.bunkers.npc.combat.sorcerer;

import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.npc.combat.BunkerNPCAbility;
import org.bukkit.Location;

public class AbilityLightningStrike extends BunkerNPCAbility {
    public AbilityLightningStrike(AbstractBunkerNPCController parent) {
        super("Lightning Strike", parent);
    }

    @Override
    public String getDescription() {
        return "Call upon thor to strike your enemies with lightning";
    }

    @Override
    public void use() {
        Location loc = getParent().getCurrentTarget().getTarget().getImmediateLocation();
        getParent().getWorld().strikeLightningEffect(loc);

        double damage = 10 * getParent().getParent().getLevel();
        Object target = getParent().getCurrentTarget().getTarget().get();
        if(target instanceof BunkerElement) {
            BunkerElement targetElement = (BunkerElement) target;
            ElementDrop drop = targetElement.getDropForDamage(damage);
            if(drop != null) {
                drop.apply(getParent().getBunker());
            }
            targetElement.damage(damage);
        } else if(target instanceof AbstractBunkerNPCController) {
            ((AbstractBunkerNPCController) target).damage(damage);
        }
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
        return 169;
    }
}
