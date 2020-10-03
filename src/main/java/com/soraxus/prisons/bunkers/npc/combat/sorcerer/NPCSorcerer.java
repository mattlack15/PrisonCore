package com.soraxus.prisons.bunkers.npc.combat.sorcerer;

import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.Target;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import com.soraxus.prisons.util.ItemBuilder;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class NPCSorcerer extends CombatNPCController {
    public NPCSorcerer(BunkerNPC parent) {
        super(parent);
        this.addAbility(new AbilityFireball(this));
        this.addAbility(new AbilityLightningStrike(this));
    }

    @Override
    public void onSpawn(NPC npc) {
        npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND,
                new ItemBuilder(Material.BLAZE_ROD, 1)
                        .addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                        .setName("&c&lNice Client :)")
                        .build());
    }

    @Override
    public Target target(AvailableTarget<?> availableTarget) {
        return new Target(availableTarget, 6);
    }
}
