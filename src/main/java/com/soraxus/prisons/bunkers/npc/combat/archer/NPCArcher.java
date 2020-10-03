package com.soraxus.prisons.bunkers.npc.combat.archer;

import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.Target;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import com.soraxus.prisons.util.ItemBuilder;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;

public class NPCArcher extends CombatNPCController {
    public static final double shootingDistance = 10D;

    public NPCArcher(BunkerNPC parent) {
        super(parent);
        this.addAbility(new AbilityShoot(this));
        if(this.getParent().getLevel() > 1) {
            this.addAbility(new AbilityArrowBarrage(this));
        }
        if(this.getParent().getLevel() > 2) {
            this.addAbility(new AbilityExplosiveArrow(this));
        }
        if(this.getParent().getLevel() > 3) {

        }
    }

    @Override
    public void onSpawn(NPC npc) {
        npc.getTrait(Equipment.class)
                .set(Equipment.EquipmentSlot.HAND,
                        new ItemBuilder(Material.BOW, 1)
                                .setName("Nice client").build()
                );
    }

    @Override
    public Target target(AvailableTarget<?> availableTarget) {
        return new Target(availableTarget, 10);
    }
}