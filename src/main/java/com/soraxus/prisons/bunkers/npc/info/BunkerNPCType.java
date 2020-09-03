package com.soraxus.prisons.bunkers.npc.info;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.combat.archer.NPCArcher;
import com.soraxus.prisons.bunkers.npc.info.types.NPCTypeArcher;
import com.soraxus.prisons.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public enum BunkerNPCType {
    ARCHER("Archer",
            new ItemBuilder(Material.BOW, 1).build(),
            "I shoot things in the face",
            1, //TODO May change
            NPCArcher::new,
            new NPCTypeArcher());


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

    BunkerNPCType(String displayName, ItemStack displayItem, String description, int requiredBarracksLevel, Function<BunkerNPC, AbstractBunkerNPCController> controllerGetter, BunkerNPCTypeInfo info) {
        this.displayName = displayName;
        this.info = info;
        this.displayItem = displayItem;
        this.description = description;
        this.controllerGetter = controllerGetter;
        this.requiredBarracksLevel = requiredBarracksLevel;
    }

    public int getGenerationTime(int level) {
        //TODO
        return info.getGenerationTimeTicks(level);
    }

    public Storage[] getCost(int level) {
        //TODO
        return info.getCost(level);
    }

    public AbstractBunkerNPCController getController(BunkerNPC npc) {
        return this.controllerGetter.apply(npc);
    }

}
