package com.soraxus.prisons.bunkers.base.shops;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.ElementWorkerHut;
import com.soraxus.prisons.bunkers.base.elements.defense.active.barracks.ElementBarracks;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementGate;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementWall;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp.ElementArmyCamp;
import com.soraxus.prisons.bunkers.base.elements.entertainment.ElementPlot;
import com.soraxus.prisons.bunkers.base.elements.generator.ElementGeneratorStone;
import com.soraxus.prisons.bunkers.base.elements.generator.ElementGeneratorTimber;
import com.soraxus.prisons.bunkers.base.elements.storage.ElementStorageStone;
import com.soraxus.prisons.bunkers.base.elements.storage.ElementStorageTimber;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.shop.BunkerShop;
import com.soraxus.prisons.bunkers.shop.BunkerShopItem;
import com.soraxus.prisons.bunkers.shop.BunkerShopSection;
import com.soraxus.prisons.bunkers.workers.TaskBuildAndEnable;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.util.ItemBuilder;
import net.ultragrav.asyncworld.SpigotAsyncWorld;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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

        //Timber Storage
        BunkerElement element = new ElementStorageTimber(bunker);
        sectionStorage.addItem(createItem(bunker, new ItemBuilder(Material.LOG, 1).setName("&eTimber Storage")
                        .addLore("&7Storage for your pieces of wood").addLore(getIntermediateMessages(bunker, tile, element)).build(),
                getSetElementCallback(bunker, element, tile)));

        BunkerShopSection sectionGenerator = new BunkerShopSection("Generation",
                new ItemBuilder(Material.IRON_PICKAXE, 1).setName("&6&lGeneration").addLore("&7Elements related to generation").build());
        //Timber Generator
        element = new ElementGeneratorTimber(null, bunker);
        sectionGenerator.addItem(
                createItem(bunker, new ItemBuilder(Material.LOG, 1).setName("&eTimber Generator")
                        .addLore("&7Generate wood automatically :D").addLore(getIntermediateMessages(bunker, tile, element)).build(),
                getSetElementCallback(bunker, element, tile), element.getType().getBuildCost(1)));

        //Stone Generator
        element = new ElementGeneratorStone(null, bunker);
        sectionGenerator.addItem(createItem(bunker, new ItemBuilder(Material.COBBLESTONE, 1).setName("&7Stone Generator")
                        .addLore("&7Generate stone automatically :D").addLore(getIntermediateMessages(bunker, tile, element)).build(),
                getSetElementCallback(bunker, element, tile), element.getType().getBuildCost(1)));

        //Stone Storage
        element = new ElementStorageStone(bunker);
        sectionStorage.addItem(createItem(bunker, new ItemBuilder(Material.COBBLESTONE, 1).setName("&eStone Storage")
                        .addLore("&7Store your rocks").addLore(getIntermediateMessages(bunker, tile, element)).build(),
                getSetElementCallback(bunker, element, tile), element.getType().getBuildCost(1)));

        BunkerShopSection sectionWorkers = new BunkerShopSection("Workers",
                new ItemBuilder(Material.WOOD, 1).setName("&6&lWorkers").addLore("&7Pretty much just contains worker huts").build());
        //Worker hut
        element = new ElementWorkerHut(bunker);
        sectionWorkers.addItem(createItem(bunker, new ItemBuilder(Material.BRICK, 1).setName("&eWorker Hut")
                        .addLore("&7Another worker!").addLore(getIntermediateMessages(bunker, tile, element)).build(),
                getSetElementCallback(bunker, element, tile)));

        BunkerShopSection sectionDefense = new BunkerShopSection("Defense",
                new ItemBuilder(Material.WOOD, 1)
                        .setName("&6&lDefense")
                        .addLore(
                                "&7Everything you need to defend your bunker",
                                "§7from those pesky attackers"
                        )
                        .build()
        );
        //Wall
        element = new ElementWall(bunker);
        sectionDefense.addItem(createItem(bunker, new ItemBuilder(Material.COBBLE_WALL, 1)
                        .setName("&eWall")
                        .addLore("&7Block your enemies out!")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                getSetElementCallback(bunker, element, tile)));

        //Gate
        element = new ElementGate(bunker);
        sectionDefense.addItem(createItem(bunker, new ItemBuilder(Material.FENCE_GATE, 1, (byte) 1)
                        .setName("&eGate")
                        .addLore("&7Block your enemies out! But with a gate!")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                getSetElementCallback(bunker, element, tile)));

        BunkerShopSection sectionArmy = new BunkerShopSection("Army",
                new ItemBuilder(Material.IRON_SWORD, 1)
                        .setName("&6&lArmy")
                        .addLore(
                                "&7Things you need to build up your army",
                                "§7and destroy your enemies"
                        )
                        .build()
        );

        element = new ElementBarracks(bunker);
        sectionArmy.addItem(createItem(bunker, new ItemBuilder(Material.IRON_CHESTPLATE, 1)
                        .setName("&eBarracks")
                        .addLore("&7Train hardened warriors!")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                getSetElementCallback(bunker, element, tile)));

        element = new ElementArmyCamp(bunker);
        sectionArmy.addItem(createItem(bunker, new ItemBuilder(Material.BANNER, 1)
                        .setName("&eArmy Camp")
                        .addLore("&7Even the toughest warriors needs sleep. Grav more than anybody.")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                getSetElementCallback(bunker, element, tile)));

        //Entertainment
        BunkerShopSection sectionEntertainment = new BunkerShopSection("Entertainment",
                new ItemBuilder(Material.WOOL, 1, (byte) 3)
        .setName("&bEntertainment")
        .addLore("&7Entertain Yourself").build());

        element = new ElementPlot(bunker);
        sectionEntertainment.addItem(createItem(bunker, new ItemBuilder(Material.CHEST, 1)
                        .setName("&ePlot")
                        .addLore("&7You know what this is")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                getSetElementCallback(bunker, element, tile)));

        //DEBUG
        element = new ElementWall(bunker);
        sectionDefense.addItem(createItem(bunker, new ItemBuilder(Material.COMMAND, 1)
                        .setName("&eUnbuild All")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                (p) -> {
                    SpigotAsyncWorld world = new SpigotAsyncWorld(bunker.getWorld().getBukkitWorld());
                    bunker.getTileMap().getElements().forEach(e -> {
                        e.unBuild(world, false);
                    });
                    world.flush();
                }));

        //DEBUG
        element = new ElementWall(bunker);
        sectionDefense.addItem(createItem(bunker, new ItemBuilder(Material.COMMAND, 1)
                        .setName("&eBuild All")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                (p) -> {
                    SpigotAsyncWorld world = new SpigotAsyncWorld(bunker.getWorld().getBukkitWorld());
                    bunker.getTileMap().getElements().forEach(e -> {
                        e.build(world, false);
                    });
                    world.flush();
                }));

        //DEBUG
        element = new ElementWall(bunker);
        sectionDefense.addItem(createItem(bunker, new ItemBuilder(Material.COMMAND, 1)
                        .setName("&eReplace All With Walls Sequentially")
                        .addLore(
                                getIntermediateMessages(bunker, tile, element)
                        )
                        .build(),
                (p) -> {
                    new BukkitRunnable() {
                        int i = 0;

                        @Override
                        public void run() {
                            BunkerElement element1 = new ElementWall(bunker);
                            if (bunker.getElement(i % 33, ((i - (i % 33)) / 33)) != null) {
                                BunkerElement element2 = bunker.getElement(i % 33, ((i - (i % 33)) / 33));
                                element2.remove();
                            }
                            bunker.setElement(i % 33, ((i - (i % 33)) / 33), element1);
                            element1.build();
                            element1.enable();
                            if (i++ == 33 * 33 - 1) {
                                Bukkit.getScheduler().cancelTask(this.getTaskId());
                                Bukkit.broadcastMessage("Finished!");
                                i = 0;
                            }
                        }
                    }.runTaskTimerAsynchronously(SpigotPrisonCore.instance, 0, 10);
                }));

        this.addSection(sectionGenerator);
        this.addSection(sectionStorage);
        this.addSection(sectionWorkers);
        this.addSection(sectionDefense);
        this.addSection(sectionArmy);
        this.addSection(sectionEntertainment);
    }

    private String[] getIntermediateMessages(Bunker bunker, IntVector2D tile, BunkerElement element) {
        return bunker.canPlace(tile.getX(), tile.getY(),
                element) ? (bunker.getFreeWorker() != null ? new String[]{"&8" + element.getShape().getX() + "x" + element.getShape().getY()} :
                new String[]{"&8" + element.getShape().getX() + "x" + element.getShape().getY(), "&cNo free worker available!"}) :
                new String[]{"&8" + element.getShape().getX() + "x" + element.getShape().getY(), "&cCannot place here (Too big)"};
    }

    private Consumer<Player> getSetElementCallback(Bunker bunker, BunkerElement element, IntVector2D tile) {
        return (p) -> {
            if (!bunker.canPlace(tile.getX(), tile.getY(), element))
                return;

            Worker worker = bunker.getFreeWorker();
            if (worker == null)
                return;

            bunker.setElement(tile, element);
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
