package com.soraxus.prisons.bunkers.base.shops;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.bunkers.shop.BunkerShop;
import com.soraxus.prisons.bunkers.shop.BunkerShopItem;
import com.soraxus.prisons.bunkers.shop.BunkerShopSection;
import com.soraxus.prisons.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class BunkerSelectiveShop extends BunkerShop {

    private final Consumer<BunkerElementType> callback;

    public BunkerSelectiveShop(Consumer<BunkerElementType> callback) {
        super("Element Shop");
        this.callback = callback;

        //Create sections
        BunkerShopSection sectionStorage = new BunkerShopSection("Storage",
                new ItemBuilder(Material.ENDER_CHEST, 1)
                        .setName("&6&lStorage")
                        .addLore("&7Elements related to storage")
                        .build()
        );

        BunkerShopSection sectionGenerator = new BunkerShopSection("Generation",
                new ItemBuilder(Material.IRON_PICKAXE, 1).setName("&6&lGeneration").addLore("&7Elements related to generation").build());

        BunkerShopSection sectionWorkers = new BunkerShopSection("Workers",
                new ItemBuilder(Material.WOOD, 1).setName("&6&lWorkers").addLore("&7Pretty much just contains worker huts").build());

        BunkerShopSection sectionDefense = new BunkerShopSection("Defense",
                new ItemBuilder(Material.WOOD, 1)
                        .setName("&6&lDefense")
                        .addLore(
                                "&7Everything you need to defend your bunker",
                                "§7from those pesky attackers"
                        )
                        .build()
        );

        BunkerShopSection sectionArmy = new BunkerShopSection("Army",
                new ItemBuilder(Material.IRON_SWORD, 1)
                        .setName("&6&lArmy")
                        .addLore(
                                "&7Things you need to build up your army",
                                "§7and destroy your enemies"
                        )
                        .build()
        );


        //Entertainment
        BunkerShopSection sectionEntertainment = new BunkerShopSection("Entertainment",
                new ItemBuilder(Material.WOOL, 1, (byte) 3)
                        .setName("&bEntertainment")
                        .addLore("&7Entertain Yourself").build());

        //Decoration
        BunkerShopSection sectionDecoration = new BunkerShopSection("Decoration", new ItemBuilder(Material.REDSTONE_LAMP_OFF, 1)
                .setName("&6&lDecoration")
                .addLore("&7Decorate your bunker!").build());

        this.addSection(sectionGenerator);
        this.addSection(sectionStorage);
        this.addSection(sectionWorkers);
        this.addSection(sectionDefense);
        this.addSection(sectionArmy);
        this.addSection(sectionEntertainment);
        this.addSection(sectionDecoration);

        for(BunkerElementType type : BunkerElementType.values()) {
            if(type.getInfo() == null)
                return;
            TypeShopInfo info = type.getInfo().getShopInfo();
            if(info == null)
                continue;

            for(BunkerShopSection section : this.getSectionList()) {
                if(section.getName().equalsIgnoreCase(info.getSection())) {
                    section.addItem(createItem(new ItemBuilder(info.getItem()).addLore("")
                                    .addLore("&6&lNOTE: &fThis is a selective menu,")
                                    .addLore("&fselecting this and left clicking an empty")
                                    .addLore("&ftile will place this").build(),
                            getSetElementCallback(type),
                            type.getBuildCost(1)));
                    break;
                }
            }
        }
    }

    private Consumer<Player> getSetElementCallback(BunkerElementType elementType) {
        return (p) -> {
            p.closeInventory();
            callback.accept(elementType);
        };
    }

    private BunkerShopItem createItem(ItemStack displayItem, Consumer<Player> giver, Storage... cost) {

        String[] costString = new String[cost.length];
        for (int i = 0, costLength = cost.length; i < costLength; i++) {
            Storage c = cost[i];
            costString[i] = c.getResource().getColor() + c.getResource().getDisplayName() + " &f" + c.getAmount();
        }
        return new BunkerShopItem(this, displayItem, () -> true, giver, costString);
    }
}
