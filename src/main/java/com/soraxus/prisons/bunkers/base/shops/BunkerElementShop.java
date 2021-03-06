package com.soraxus.prisons.bunkers.base.shops;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.elements.type.TypeShopInfo;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.shop.BunkerShop;
import com.soraxus.prisons.bunkers.shop.BunkerShopItem;
import com.soraxus.prisons.bunkers.shop.BunkerShopSection;
import com.soraxus.prisons.bunkers.workers.TaskBuildAndEnable;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.items.ItemBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BunkerElementShop extends BunkerShop {
    public BunkerElementShop(Bunker bunker, IntVector2D tile) {
        super("Element Shop");

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
                                "??7from those pesky attackers"
                        )
                        .build()
        );

        BunkerShopSection sectionArmy = new BunkerShopSection("Army",
                new ItemBuilder(Material.IRON_SWORD, 1)
                        .setName("&6&lArmy")
                        .addLore(
                                "&7Things you need to build up your army",
                                "??7and destroy your enemies"
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

        for (BunkerElementType type : BunkerElementType.values()) {
            if (type.getInfo() == null)
                return;
            TypeShopInfo info = type.getInfo().getShopInfo();
            if (info == null)
                continue;

            for (BunkerShopSection section : this.getSectionList()) {
                if (section.getName().equalsIgnoreCase(info.getSection())) {
                    section.addItem(createItem(bunker, new ItemBuilder(info.getItem()).addLore("").addLore(getIntermediateMessages(bunker, tile, type)).build(),
                            getSetElementCallback(bunker, type, tile),
                            type.getBuildCost(1)));
                    break;
                }
            }
        }
    }

    private String[] getIntermediateMessages(Bunker bunker, IntVector2D tile, BunkerElementType element) {
        return bunker.getTileMap().canPlace(tile.getX(), tile.getY(),
                element.getBaseLevelShape()) ? (bunker.getFreeWorker() != null ? new String[]{"&8" + element.getBaseLevelShape().getX() + "x" + element.getBaseLevelShape().getY()} :
                new String[]{"&8" + element.getBaseLevelShape().getX() + "x" + element.getBaseLevelShape().getY(), "&cNo free worker available!"}) :
                new String[]{"&8" + element.getBaseLevelShape().getX() + "x" + element.getBaseLevelShape().getY(), "&cCannot place here (Too big)"};
    }

    private Consumer<Player> getSetElementCallback(Bunker bunker, BunkerElementType elementType, IntVector2D tile) {
        return (p) -> {
            if (!bunker.getTileMap().canPlace(tile.getX(), tile.getY(), elementType.getBaseLevelShape()))
                return;

            Worker worker = bunker.getFreeWorker();
            if (worker == null)
                return;

            Storage[] buildCost = elementType.getBuildCost(0);
            if (!bunker.hasResources(buildCost)) {
                List<String> strs = new ArrayList<>();
                for (Storage storage : buildCost) {
                    String str = "";
                    Storage owned = bunker.getCombinedStorages().get(storage.getResource());
                    if (owned.getAmount() < storage.getAmount()) {
                        str += "&c";
                    } else {
                        str += "&a";
                    }
                    str += owned.getAmount() + " / " + storage.getAmount();
                    strs.add(str);
                }
                HoverEvent hover = HoverUtil.text(String.join("\n", strs));
                new ChatBuilder()
                        .addText("&cYou do not have enough resources to build that! (Hover for details)", hover)
                        .send(p);
                return;
            }
            bunker.removeResources(buildCost);
            bunker.setElement(tile, elementType.getConstructor().createElement(bunker));
            TaskBuildAndEnable task = new TaskBuildAndEnable(bunker.getTileMap().getTile(tile), worker);
            try {
                task.start();
                p.closeInventory();
            } catch (IllegalStateException ignored) {
            }
        };
    }

    private BunkerShopItem createItem(Bunker bunker, ItemStack displayItem, Consumer<Player> giver, Storage... cost) {
        Map<BunkerResource, Double> resources = bunker.getResources();

        String[] costString = new String[cost.length];
        for (int i = 0, costLength = cost.length; i < costLength; i++) {
            Storage c = cost[i];
            if (resources.get(c.getResource()) < c.getAmount()) {
                costString[i] = c.getResource().getColor() + c.getResource().getDisplayName() + "&c" + c.getAmount();
            } else {
                costString[i] = c.getResource().getColor() + c.getResource().getDisplayName() + " &f" + c.getAmount();
            }
        }
        return new BunkerShopItem(this, displayItem, () -> testRequirement(bunker, cost), giver, costString);
    }

    private boolean testRequirement(Bunker bunker, Storage... cost) {
        for (Storage c : cost) {
            if (bunker.getResourceAmount(c.getResource()) < c.getAmount()) {
                return false;
            }
        }
        return true;
    }
}
