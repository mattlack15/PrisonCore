package com.soraxus.prisons.bunkers.npc.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.combat.archer.NPCArcher;
import com.soraxus.prisons.bunkers.npc.combat.bomber.NPCBomber;
import com.soraxus.prisons.bunkers.npc.combat.sorcerer.NPCSorcerer;
import com.soraxus.prisons.bunkers.npc.info.types.NPCTypeArcher;
import com.soraxus.prisons.bunkers.npc.info.types.NPCTypeBomber;
import com.soraxus.prisons.bunkers.npc.info.types.NPCTypeSorcerer;
import com.soraxus.prisons.util.items.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Function;

@AllArgsConstructor
public enum BunkerNPCType {
    ARCHER("Archer",
            new ItemBuilder(Material.BOW, 1).setName("&eArcher").build(),
            "I shoot things in the face",
            1,
            NPCArcher::new,
            new NPCTypeArcher()),

    BOMBER("Boomer",
            new ItemBuilder(Material.TNT, 1).setName("&eBoomer").build(),
            "I blow things into small pieces",
            1,
            NPCBomber::new,
            new NPCTypeBomber()),

    SORCERER("Sorcerer",
            new ItemBuilder(Material.BLAZE_ROD, 1).setName("&eSorcerer").build(),
            "I do magic trickz",
            1,
            NPCSorcerer::new,
            new NPCTypeSorcerer());


    @Getter
    private final String displayName;
    @Getter
    private final ItemStack displayItem;
    @Getter
    private final String description;
    @Getter
    private final int requiredBarracksLevel;
    private final Function<BunkerNPC, AbstractBunkerNPCController> controllerGetter;
    @Getter
    private final BunkerNPCTypeInfo info;

    public int getGenerationTime(int level) {
        return info.getGenerationTimeTicks(level);
    }

    public int getUpgradeTime(int currentLevel) {
        return getGenerationTime(currentLevel) * 5;
    }

    public Storage[] getCost(int level) {
        return info.getCost(level);
    }

    public Storage[] getUpgradeCost(int currentLevel) {
        Storage[] cost = getCost(currentLevel);
        cost = Arrays.copyOf(cost, cost.length);
        for (int i = 0, costLength = cost.length; i < costLength; i++) {
            Storage storage = cost[i];
            cost[i] = new Storage(storage.getResource(), storage.getAmount() * 100, 0);
        }
        return cost;
    }

    public AbstractBunkerNPCController getController(BunkerNPC npc) {
        return this.controllerGetter.apply(npc);
    }

}
