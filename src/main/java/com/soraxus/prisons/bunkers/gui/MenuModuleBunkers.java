package com.soraxus.prisons.bunkers.gui;

import com.soraxus.prisons.bunkers.BunkerDebugStats;
import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class MenuModuleBunkers extends Menu {

    private MenuElement backButton;

    public MenuModuleBunkers(MenuElement backButton) {
        super("Bunkers", 5);
        this.backButton = backButton;
        this.setup();
    }

    public void setup() {
        MenuElement stats = new MenuElement(new ItemBuilder(Material.BOOK).setName("&c&lStats")
                .addLore("&8Avg. load time: &f" +
                                MathUtils.round(BunkerDebugStats.measureMap.get(BunkerDebugStats.DebugStat.BUNKER_LOAD_TOTAL).getAvg(), 1)
                                + "ms",
                        "&8Loaded Bunkers: &f" + BunkerManager.instance.getLoadedBunkers().size(),
                        "&8Total Bunkers: &f" + (BunkerManager.instance.getBaseDir().listFiles().length - 1)).build());


        List<Bunker> loadedBunkers = BunkerManager.instance.getLoadedBunkers();
        this.setupActionableList(19, 19 + 7 + 8, 19 + 7 + 8 + 2, 19 + 7 + 8 + 2 + 8,
                (index) -> {
                    if (index >= loadedBunkers.size())
                        return null;
                    Bunker bunker = loadedBunkers.get(index);
                    return new MenuElement(new ItemBuilder(Material.IRON_BLOCK, 1)
                            .setName("&9&l" + bunker.getGang().getName())
                            .addLore("&8Rating: &f" + bunker.getRating(),
                                    "&8Members online: &f" + bunker.getGang().getMembers().stream().map(m -> Bukkit.getPlayer(m.getMember()))
                                            .filter(Objects::nonNull).count(),
                                    "&8Attacking: &f" + (bunker.getAttackingMatch() == null ? "noone" : bunker.getAttackingMatch().getDefender().getGang().getName()),
                                    "&8Being attacked by: &f" + (bunker.getDefendingMatch() == null ? "noone" : bunker.getDefendingMatch().getAttacker().getGang().getName()),
                                    "",
                                    "&8Click to teleport").build())
                            .setClickHandler((e, i) -> {
                                Bunker bunker1 = BunkerManager.instance.getLoadedBunker(bunker.getId()); //Make sure it's still loaded
                                if (bunker1 != null) {
                                    e.getWhoClicked().closeInventory();
                                    bunker1.teleport((Player) e.getWhoClicked());
                                } else {
                                    setup();
                                }
                            });
                }, 0);

        this.setElement(4, stats);
        this.setElement(0, backButton);

        MenuManager.instance.invalidateInvsForMenu(this);

    }
}
