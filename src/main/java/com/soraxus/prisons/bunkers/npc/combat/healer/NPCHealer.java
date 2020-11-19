package com.soraxus.prisons.bunkers.npc.combat.healer;

import com.soraxus.prisons.bunkers.npc.*;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NPCHealer extends CombatNPCController {
    public NPCHealer(BunkerNPC parent) {
        super(parent);
        this.setTargetingType(TargetType.FRIENDLY);
        this.addAbility(new AbilityHealSingle(this));
        this.addAbility(new AbilityHealGroup(this));
    }

    @Override
    public void onSpawn(NPC npc) {
        npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.SPLASH_POTION)); //Make it a healing potion
    }

    @Override
    public Target target(AvailableTarget<?> availableTarget) {
        return new Target(availableTarget, 8);
    }

    @Override
    public List<AvailableTarget<?>> getAvailableTargets() {
        List<AvailableTarget<?>> targetList = super.getAvailableTargets();
        targetList.removeIf((t -> t instanceof ElementAvailableTarget)); //Only target other NPCs
        return targetList;
    }
}

