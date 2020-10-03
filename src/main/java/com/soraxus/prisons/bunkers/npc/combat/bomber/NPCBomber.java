package com.soraxus.prisons.bunkers.npc.combat.bomber;

import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.Target;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import com.soraxus.prisons.util.ItemBuilder;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;

public class NPCBomber extends CombatNPCController {
    public NPCBomber(BunkerNPC parent) {
        super(parent);
        this.addAbility(new AbilityBlowUp(this));
    }

    @Override
    public void onSpawn(NPC npc) {
        npc.getTrait(Equipment.class)
                .set(Equipment.EquipmentSlot.HAND,
                        new ItemBuilder(Material.TNT, 1)
                                .build()
                );
    }

    @Override
    public Target target(AvailableTarget<?> availableTarget) {
        return new Target(availableTarget, 2);
    }
}
