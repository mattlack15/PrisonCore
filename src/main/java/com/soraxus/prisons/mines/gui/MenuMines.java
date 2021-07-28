package com.soraxus.prisons.mines.gui;

import com.soraxus.prisons.mines.MineCreationSession;
import com.soraxus.prisons.mines.ModuleMines;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

public class MenuMines extends Menu {

    private final MenuElement backButton;

    private final Mine currentMine;

    public MenuMines(MenuElement backButton, Mine currentMine) {
        super("Mines", 5);
        this.backButton = backButton;
        this.currentMine = currentMine;
        this.setup();
    }

    public void setup() {
        List<Mine> mines = MineManager.instance.getLoaded();
        mines.sort(Comparator.comparingInt(Mine::getOrder));

        this.setElement(4, backButton);

//        this.setElement(6, new MenuElement(new ItemBuilder(Material.LEAVES, 1)
//                .setName("Convert")
//                .addLore("Convert from MineResetLite").build()).setClickHandler((e, i) -> {
//            //Convert
//            File folder = new File(ModuleMines.instance.getDataFolder(), "litemines");
//
//            for (File file : Objects.requireNonNull(folder.listFiles())) {
//                if (!file.getName().endsWith(".yml"))
//                    continue;
//
//                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//                    List<String> contents = new ArrayList<>();
//                    String str;
//                    int removedLines = 0;
//                    while ((str = reader.readLine()) != null) {
//                        System.out.println(str);
//                        if (!str.contains("com.koletar.jj.mineresetlite.Mine")) {
//                            contents.add(str);
//                            System.out.println(str);
//                        } else {
//                            removedLines ++;
//                            System.out.println("Found line to remove");
//                        }
//                    }
//                    reader.close();
//                    FileWriter writer = new FileWriter(file);
//                    System.out.println("Removed " + removedLines + " lines!");
//                    for (String str2 : contents) {
//                        writer.write(str2 + "\n");
//                    }
//                    writer.flush(); // Flush the writer to write all data to the file
//                    writer.close();
//                } catch(IOException e1) {
//                    e1.printStackTrace();
//                }
//
//                Bukkit.broadcastMessage("Converting mine: " + file.getName());
//
//                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
//                System.out.println(config.saveToString());
//                ConfigurationSection section = config.getConfigurationSection("mine");
//                double minX = section.getInt("minX");
//                double minY = section.getInt("minY");
//                double minZ = section.getInt("minZ");
//                double maxX = section.getInt("maxX");
//                double maxY = section.getInt("maxY");
//                double maxZ = section.getInt("maxZ");
//
//                Vector min = new Vector(minX, minY, minZ);
//                Vector max = new Vector(maxX, maxY, maxZ);
//
//                World world = Bukkit.getWorld(section.getString("world"));
//                if (world == null)
//                    continue;
//
//                String name = section.getString("name");
//
//                Map<Integer, Double> blocks = new HashMap<>();
//                ConfigurationSection composition = section.getConfigurationSection("composition");
//                for (String keys : composition.getKeys(false)) {
//                    String[] split = keys.split(":");
//                    if (split.length == 1) {
//                        blocks.put(Integer.parseInt(split[0]), composition.getDouble(keys));
//                    } else if (split.length == 2) {
//                        blocks.put(Integer.parseInt(split[1]) << 12 | Integer.parseInt(split[0]), composition.getDouble(keys));
//                    }
//                }
//
//                Mine mine = new Mine(name, new CuboidRegion(world, min, max));
//                blocks.forEach(mine::addMineBlock);
//
//                MineManager.instance.add(mine);
//            }
//
//            this.setup();
//        }));

        this.setElement(2, new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&eCreate").build()).setClickHandler((e, i) -> {
            new MineCreationSession((Player) e.getWhoClicked(), (mine) -> {
                if(!MineManager.instance.add(mine)) {
                    e.getWhoClicked().sendMessage(ChatColor.RED + "Unable to create mine, one already exists with that name! (" + mine.getName() + ")");
                }
            });
            e.getWhoClicked().closeInventory();
        }));

        if(this.currentMine != null) {
            this.setElement(6, new MenuElement(new ItemBuilder(getItemForMine(currentMine)).setName("&a&lCurrent Mine &7(&e" + currentMine.getName() + "&7)").build())
                    .setClickHandler((e, i) -> new MenuMine(currentMine, getBackButton(this).setClickHandler((e1, i1) -> {
                        this.setup();
                        this.open(e1.getWhoClicked());
                    })).open(e.getWhoClicked())).setDoUpdates(true).setUpdateEvery(10).setUpdateHandler((element) -> element.setItem(getItemForMine(currentMine))));
        }

        this.setupActionableList(10, 9 * 4 - 2, 9 * 4, 9 * 5 - 1, (index) -> {
            if (index >= mines.size())
                return null;

            Mine mine = mines.get(index);
            return new MenuElement(getItemForMine(mine))
                    .setClickHandler((e, i) -> new MenuMine(mine, getBackButton(this).setClickHandler((e1, i1) -> {
                        this.setup();
                        this.open(e1.getWhoClicked());
                    })).open(e.getWhoClicked())).setDoUpdates(true).setUpdateEvery(10).setUpdateHandler((element) -> element.setItem(getItemForMine(mine)));
        }, 0);

    }

    public ItemStack getItemForMine(Mine mine) {
        double threshold = ModuleMines.instance.getMinedThreshold();
        double percentage = mine.getBlocksMined() / (mine.getMineArea() * threshold);
        Material m = Material.EMERALD_BLOCK;
        if (percentage > 0.5) {
            m = Material.GOLD_BLOCK;
        }
        if (percentage > 0.75) {
            m = Material.REDSTONE_BLOCK;
        }
        ItemBuilder builder = new ItemBuilder(m, 1);
        builder.setName("&e&l" + mine.getName());
        builder.addLore("&fOrder: &7" + mine.getOrder());
        builder.addLore("&fPercentage Mined: &7" + Math.round(mine.getBlocksMined() / (double) mine.getMineArea() * 100) + "%");
        builder.addLore("&fPermission: &7" + mine.getPermission());
        builder.addLore("&fUnique Blocks: &7" + mine.getBlocks().size());
        builder.addLore("&fTime till reset: &7" + (int)Math.max((20 * 60 - (System.currentTimeMillis() - mine.getLastMinedBlock().get()) / 1000D), 0) + "s");
        return builder.build();
    }
}
