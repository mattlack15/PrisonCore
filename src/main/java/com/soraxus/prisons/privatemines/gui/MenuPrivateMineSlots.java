package com.soraxus.prisons.privatemines.gui;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.privatemines.MineVisitationManager;
import com.soraxus.prisons.privatemines.MineVisitor;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.VisitationType;
import com.soraxus.prisons.util.*;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
        MineVisitationManager manager = mine.getVisitationManager();

        MenuElement rentElement = new MenuElement(new ItemBuilder(Material.DOUBLE_PLANT).setName("&c&lRent")
        .addLore("&fCurrently: &7" + NumberUtils.toReadableNumber(manager.getRentalPrice()))
        .addLore("", "&8Click to change").build()).setClickHandler((e, i) -> {
            listeners.put(e.getWhoClicked().getUniqueId(), this);
            ((Player)e.getWhoClicked()).sendTitle(ChatColor.RED + "Type the new rent in chat", "please ;)");
            e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "!! " + ChatColor.GOLD + "Enter the new rent price here:");
            e.getWhoClicked().closeInventory();
        });

        List<MineVisitor> renting = mine.getVisitationManager().getVisitorsOfType(VisitationType.RENTAL);
        List<MineVisitor> members = mine.getVisitationManager().getVisitorsOfType(VisitationType.FREE);
        int rentingSlots = mine.getVisitationManager().getAtomicRentedSlots().get();
        int memberSlots = mine.getVisitationManager().getNonRentedSlots();

        AtomicInteger listPage1 = new AtomicInteger(page1);
        AtomicInteger listPage2 = new AtomicInteger(page2);

        ItemBuilder builder1 = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 2);
        builder1.setName("&eEmpty Slot");
        builder1.addLore("&fClick to make this slot a&a member slot");
        MenuElement rentableSlot = new MenuElement(builder1.build()).setClickHandler((e, i) -> {
            manager.getAtomicRentedSlots().getAndUpdate((c) -> {
                if(c <= 0)
                    return c;
                return c-1;
            });
            setup(listPage1.get(), listPage2.get());
        });

        builder1 = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 2);
        builder1.setName("&eEmpty Slot");
        builder1.addLore("&fClick to make this slot&c rentable");
        MenuElement memberSlot = new MenuElement(builder1.build()).setClickHandler((e, i) -> {
            manager.getAtomicRentedSlots().getAndUpdate((c) -> {
                if(c >= manager.getSlots())
                    return manager.getSlots();
                return c+1;
            });
            setup(listPage1.get(), listPage2.get());
        });

        //First list (members)
        this.setupActionableList(10, 10 + 6 + (9 * 2), 10 + 6 + (9 * 2) - 7, 10 + 6 + (9 * 2) + 1,
                (index) -> {
                    if (index >= memberSlots)
                        return null;
                    if(index < members.size()) {
                        MineVisitor visitor = members.get(index);
                        Player player = Bukkit.getPlayer(visitor.getVisitor());
                        ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM).setupAsSkull(player.getName());
                        builder.setName("&a" + player.getName());
                        return new MenuElement(builder.build());
                    } else {
                        return memberSlot;
                    }
                }, listPage1);

        //Second list (rentable)
        this.setupActionableList(10 + 18, 10 + 6 + (9 * 2) + 18, 10 + 6 + (9 * 2) - 7 + 18, 10 + 6 + (9 * 2) + 1 + 18,
                (index) -> {
                    if (index >= rentingSlots)
                        return null;
                    if(index < renting.size()) {
                        MineVisitor visitor = renting.get(index);
                        Player player = Bukkit.getPlayer(visitor.getVisitor());
                        ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM).setupAsSkull(player.getName());
                        builder.setName("&a" + player.getName());
                        builder.addLore("&fRenting at: &e$" + manager.getRentalPrice(), "");
                        builder.addLore("&cRight Click to kick");
                        return new MenuElement(builder.build()).setClickHandler((e, i) -> {
                            if(e.getClick().equals(ClickType.RIGHT)) {
                                //Kick player
                                if(mine.getVisitationManager().getVisitor(player.getUniqueId()) != null) {
                                    player.teleport(SpigotPrisonCore.instance.getSpawn());
                                }
                                setup(listPage1.get(), listPage2.get());
                            }
                        });
                    } else {
                        return rentableSlot;
                    }
                }, listPage2);

        this.setElement(4, backButton);
        this.setElement(6, rentElement);
        MenuManager.instance.invalidateInvsForMenu(this);
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        if(listeners.containsKey(event.getPlayer().getUniqueId()) && listeners.get(event.getPlayer().getUniqueId()).equals(this)) {
            listeners.remove(event.getPlayer().getUniqueId());
            String rent = event.getMessage();
            event.setCancelled(true);
            try {
                long r = Long.parseLong(rent);
                this.mine.getVisitationManager().setRentalPrice(r);
            } catch(NumberFormatException e) {
                event.getPlayer().sendMessage(ChatColor.RED + "That was not a number.. ??");
            }
            Synchronizer.synchronize(() -> {
                this.setup(0, 0);
                this.open(event.getPlayer());
            });
        }
    }
}
