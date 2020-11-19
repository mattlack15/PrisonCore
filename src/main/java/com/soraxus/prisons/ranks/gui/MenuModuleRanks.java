package com.soraxus.prisons.ranks.gui;

import com.soraxus.prisons.ranks.Rank;
import com.soraxus.prisons.ranks.RankupManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MenuModuleRanks extends Menu {

    private final MenuElement backElement;

    private static final Map<UUID, Consumer<String>> listeners = new ConcurrentHashMap<>();

    public MenuModuleRanks(MenuElement backElement) {
        super("Prison Ranks", 5);
        EventSubscriptions.instance.subscribe(this);
        this.backElement = backElement;
        this.setup();
    }

    public void setup() {

        this.setElement(0, backElement);

        this.setElement(4, new MenuElement(new ItemBuilder(Material.ANVIL).setName("&6&lCreate").addLore("&7Click to create a prison rank").build())
                .setClickHandler((e, i) -> {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter the name of the rank in chat:");
                    listeners.put(e.getWhoClicked().getUniqueId(), (s) -> {
                        Rank rank = new Rank(s, s, 0L, new ArrayList<>());
                        if (!RankupManager.instance.rankExists(s)) {
                            RankupManager.instance.addRank(rank);
                        } else {
                            e.getWhoClicked().sendMessage(ChatColor.RED + s + " is already a rank!");
                        }
                        this.setup();
                        this.open(e.getWhoClicked());
                    });
                }));

        List<Rank> ranks = RankupManager.instance.getRanks();
        this.setupActionableList(9, 9 * 4 - 1, 9 * 4, 9 * 4 + 8, (index) -> {
            if (index >= ranks.size())
                return null;

            Rank rank = ranks.get(index);

            ItemBuilder builder = new ItemBuilder(Material.STAINED_GLASS, 1, (byte) 5)
                    .setName("&f&l" + rank.getName())
                    .addLore("&7Display: &f" + rank.getDisplayName() + " &7Left click to set", "&7Cost: &f" + NumberUtils.toReadableNumber(rank.getCost()) + " &7Right click to set");

            if(rank.getCmds().size() != 0) {
                builder.addLore("", "&eCommands");
                for (String cmd : rank.getCmds()) {
                    builder.addLore("&7- /&f" + cmd);
                }
            }

            builder.addLore("", "&eNOTE &8that the cost is the cost to", "&8upgrade TO the rank", "", "&cShift Click to delete");


            return new MenuElement(builder.build()).setClickHandler((e, i) -> {
                if (e.getClick().isShiftClick()) {
                    RankupManager.instance.removeRank(rank.getName());
                } else {
                    if (e.getClick().isLeftClick()) {
                        listeners.put(e.getWhoClicked().getUniqueId(), (s) -> {
                            rank.setDisplayName(s);
                            setup();
                            open(e.getWhoClicked());
                        });
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter display name:");
                    } else if (e.getClick().isRightClick()) {
                        listeners.put(e.getWhoClicked().getUniqueId(), (s) -> {
                            try {
                                rank.setCost(Long.parseLong(s));
                            } catch (NumberFormatException e1) {
                                e.getWhoClicked().sendMessage(ChatColor.RED + "That wasn't a number... or it was too long");
                            }
                            setup();
                            open(e.getWhoClicked());
                        });
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter the new cost (how much it costs to rank up TO the rank):");
                    }
                }
                this.setup();
            });
        }, 0);

        MenuManager.instance.invalidateInvsForMenu(this);
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> consumer = listeners.remove(event.getPlayer().getUniqueId());
        if (consumer != null) {
            consumer.accept(event.getMessage());
            event.setCancelled(true);
        }
    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        listeners.remove(event.getPlayer().getUniqueId());
    }
}
