package com.soraxus.prisons.mines.gui;

import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.ranks.RankupManager;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MenuWarpsMines extends Menu {
    private MenuElement backButton;
    private UUID player;

    public MenuWarpsMines(UUID player, MenuElement backButton) {
        super("mines", 6);
        this.player = player;
        this.backButton = backButton;
        this.setup();
    }

    public void setup() {
        if (Bukkit.getPlayer(player) == null)
            return;
        this.setAll(null);
        this.setElement(4, backButton);
        List<Mine> mines = MineManager.instance.getLoaded();
        mines.sort(Comparator.comparingInt(Mine::getOrder));
        this.setupActionableList(9, 44, 45, 45 + 8, (index) -> {
            if (index >= mines.size()) {
                return null;
            }

            Mine mine = mines.get(index);

            boolean canAccess = RankupManager.instance.getPlayer(player).getRankIndex() >= mine.getOrder();

            ItemBuilder builder = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) (canAccess ? 0 : 15));
            builder.setName("&d&l" + mine.getName());
            if (canAccess) {
                builder.addLore("&7Click to teleport");
            } else {
                builder.addLore("&cYou don't have access to this mine.");
            }
            return new MenuElement(builder.build()).setClickHandler((e, i) -> {
                if (!canAccess)
                    return;
                Player p = Bukkit.getPlayer(player);
                p.performCommand("warp " + mine.getName());
            });
        }, 0);
    }
}
