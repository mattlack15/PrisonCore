package com.soraxus.prisons.privatemines.gui;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.privatemines.MineVisitationManager;
import com.soraxus.prisons.privatemines.MineVisitor;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.VisitationType;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuPrivateMineSlots extends Menu {
    private static final Map<UUID, Object> listeners = new ConcurrentHashMap<>();
    private final PrivateMine mine;
    private final MenuElement backButton;

    public MenuPrivateMineSlots(PrivateMine mine, MenuElement backButton) {
        super("Mine Slots", 5);
        this.mine = mine;
        this.backButton = backButton;
        EventSubscriptions.instance.subscribe(this);
        this.setup(0, 0);
    }

    public void setup(int page1, int page2) {
        MineVisitationManager manager = this.mine.getVisitationManager();
        MenuElement rentElement = (new MenuElement((new ItemBuilder(Material.DOUBLE_PLANT)).setName("&c&lRent").addLore("&fCurrently: &7" + NumberUtils.toReadableNumber(manager.getRentalPrice())).addLore("", "&8Click to change").build())).setClickHandler((e, i) -> {
            listeners.put(e.getWhoClicked().getUniqueId(), this);
            ((Player)e.getWhoClicked()).sendTitle(ChatColor.RED + "Type the new rent in chat", "please ;)");
            e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "!! " + ChatColor.GOLD + "Enter the new rent price here:");
            e.getWhoClicked().closeInventory();
        });
        List<MineVisitor> renting = this.mine.getVisitationManager().getVisitorsOfType(VisitationType.RENTAL);
        List<MineVisitor> members = this.mine.getVisitationManager().getVisitorsOfType(VisitationType.FREE);
        int rentingSlots = this.mine.getVisitationManager().getAtomicRentedSlots().get();
        int memberSlots = this.mine.getVisitationManager().getNonRentedSlots();
        AtomicInteger listPage1 = new AtomicInteger(page1);
        AtomicInteger listPage2 = new AtomicInteger(page2);
        ItemBuilder builder1 = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte)2);
        builder1.setName("&eEmpty Slot");
        builder1.addLore("&fClick to make this slot a&a member slot");
        MenuElement rentableSlot = (new MenuElement(builder1.build())).setClickHandler((e, i) -> {
            manager.getAtomicRentedSlots().getAndUpdate((c) -> c <= 0 ? c : c - 1);
            this.setup(listPage1.get(), listPage2.get());
        });
        builder1 = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte)6);
        builder1.setName("&eEmpty Slot");
        builder1.addLore("&fClick to make this slot&c rentable");
        MenuElement memberSlot = (new MenuElement(builder1.build())).setClickHandler((e, i) -> {
            manager.getAtomicRentedSlots().getAndUpdate((c) -> c >= manager.getSlots() ? manager.getSlots() : c + 1);
            this.setup(listPage1.get(), listPage2.get());
        });
        this.setupActionableList(10, 25, 18, 26, (index) -> {
            if (index >= memberSlots) {
                return null;
            } else if (index < members.size()) {
                MineVisitor visitor = members.get(index);
                Player player = Bukkit.getPlayer(visitor.getVisitor());
                ItemBuilder builder = (new ItemBuilder(Material.SKULL_ITEM)).setupAsSkull(player.getName());
                builder.setName("&a" + player.getName());
                return new MenuElement(builder.build());
            } else {
                return memberSlot;
            }
        }, listPage1);
        this.setupActionableList(28, 43, 36, 44, (index) -> {
            if (index >= rentingSlots) {
                return null;
            } else if (index < renting.size()) {
                MineVisitor visitor = renting.get(index);
                Player player = Bukkit.getPlayer(visitor.getVisitor());
                ItemBuilder builder = (new ItemBuilder(Material.SKULL_ITEM)).setupAsSkull(player.getName());
                builder.setName("&a" + player.getName());
                builder.addLore("&fRenting at: &e$" + manager.getRentalPrice(), "");
                builder.addLore("&cRight Click to kick");
                return (new MenuElement(builder.build())).setClickHandler((e, i) -> {
                    if (e.getClick().equals(ClickType.RIGHT)) {
                        if (this.mine.getVisitationManager().getVisitor(player.getUniqueId()) != null) {
                            player.teleport(SpigotPrisonCore.instance.getSpawn());
                        }

                        this.setup(listPage1.get(), listPage2.get());
                    }

                });
            } else {
                return rentableSlot;
            }
        }, listPage2);
        this.setElement(4, this.backButton);
        this.setElement(6, rentElement);
        MenuManager.instance.invalidateInvsForMenu(this);
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        Object c = listeners.remove(event.getPlayer().getUniqueId());
        if (c != null && c.equals(this)) {
            String rent = event.getMessage();
            event.setCancelled(true);

            try {
                long r = Long.parseLong(rent);
                this.mine.getVisitationManager().setRentalPrice(r);
            } catch (NumberFormatException var6) {
                event.getPlayer().sendMessage(ChatColor.RED + "That was not a number.. ??");
            }

            Synchronizer.synchronize(() -> {
                this.setup(0, 0);
                this.open(event.getPlayer());
            });
        }

    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        listeners.remove(event.getPlayer().getUniqueId());
    }
}