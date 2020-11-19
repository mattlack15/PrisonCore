package com.soraxus.prisons.crate.gui;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.InvInfo;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import com.soraxus.prisons.util.string.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class MenuEditCrate extends Menu {
    private Crate crate;

    private Consumer<String> chatConsumer = null;

    private MenuElement backElement;
    private UUID chatConsumerPlayer = null;
    private boolean settingDisplayItem = false;

    public MenuEditCrate(Crate crate, MenuElement backElement) {
        super("Editing " + crate.getDisplayName(), 5);
        this.crate = crate;
        this.backElement = backElement;
        EventSubscriptions.instance.subscribe(this);
        this.setup();
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        if (chatConsumer != null && event.getPlayer().getUniqueId().equals(chatConsumerPlayer)) {
            event.setCancelled(true);
            chatConsumer.accept(event.getMessage());
            chatConsumer = null;
            chatConsumerPlayer = null;
        }
    }

    @EventSubscription(priority = EventPriority.LOW)
    private void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        InvInfo info = MenuManager.instance.getInfo(event.getWhoClicked().getUniqueId());
        if (info.getCurrentInv() == null)
            return;
        if (view != null && view.getTopInventory() != null && view.getTopInventory().equals(info.getCurrentInv()) && this.equals(info.getCurrentMenu())) {

            ItemStack stack = event.getCurrentItem();
            if (stack != null && stack.getType() != Material.AIR) {

                if (event.getClickedInventory().equals(info.getCurrentInv()))
                    return;

                if (event.getView().getTopInventory().equals(event.getClickedInventory()) || event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                    if (settingDisplayItem) {
                        event.setCancelled(true);
                        crate.setItemMaterial(stack.getType());
                        crate.setItemData(stack.getData().getData());
                        settingDisplayItem = false;
                        this.setup();
                    }
                }
            }
        }
    }


    public void setup() {
        MenuElement rename = new MenuElement(new ItemBuilder(Material.NAME_TAG, 1).setName("&a&lDisplay Name").addLore("&fCurrent: " + crate.getDisplayName()).addLore("").addLore("&fClick to change")
                .build()).setClickHandler((e, i) -> {
            e.getWhoClicked().closeInventory();
            e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter the new Display Name in chat");
            chatConsumer = (s) -> {
                crate.setDisplayName(s);
                CrateManager.instance.saveCrate(crate);
                this.setup();
                this.open((Player) e.getWhoClicked());
                MenuCrates.noGC.remove(this);
            };
            MenuCrates.noGC.add(this);
            this.chatConsumerPlayer = e.getWhoClicked().getUniqueId();
        });

        MenuElement give = new MenuElement(
                new ItemBuilder(Material.EMERALD, 1)
                        .setName("&a&lGive")
                        .addLore("")
                        .addLore("&fClick to give yourself this crate")
                        .build())
                .setClickHandler((e, i) -> {
                    e.getWhoClicked().getInventory().addItem(crate.getItem());
                });

        ItemBuilder builder = new ItemBuilder(Material.PAPER, 1).setName("&a&lDescription");
        for (String descLine : TextUtil.splitIntoLines(crate.getDescription(), 30))
            builder.addLore("&7" + descLine);
        builder.addLore("");
        builder.addLore("&fClick to Change");

        MenuElement description = new MenuElement(builder.build()).setClickHandler((e, i) -> {
            e.getWhoClicked().closeInventory();
            e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter the new Description in chat");
            chatConsumer = (s) -> {
                crate.setDescription(s);
                CrateManager.instance.saveCrate(crate);
                this.setup();
                this.open((Player) e.getWhoClicked());
                MenuCrates.noGC.remove(this);
            };
            MenuCrates.noGC.add(this);
            this.chatConsumerPlayer = e.getWhoClicked().getUniqueId();
        });

        MenuElement delete = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&4&lDelete").addLore("&7Click to delete this crate")
                .build()).setClickHandler((e, i) -> {
            CrateManager.instance.remove(crate.getName());
            backElement.getClickHandler().handleClick(e, i);
        });

        MenuElement editRewards = new MenuElement(new ItemBuilder(Material.DIAMOND, 1).setName("&b&lRewards").addLore("").addLore("&fClick to edit rewards").build())
                .setClickHandler((e, i) -> new MenuEditRewards(crate, getBackButton(this).setClickHandler((e1, i1) -> {
                    setup();
                    open((Player) e1.getWhoClicked());
                })).open((Player) e.getWhoClicked()));

        ItemBuilder builder1;
        if (settingDisplayItem) {
            builder1 = new ItemBuilder(Material.GLASS_BOTTLE, 1).setName("&cWaiting...").addLore("&fClick on an item in your inventory");
        } else {
            builder1 = new ItemBuilder(crate.getItemMaterial(), 1, crate.getItemData()).setName("&6&lDisplay Item").addLore("").addLore("&fClick to Change");
        }
        MenuElement editDisplayItem = new MenuElement(builder1.build()).setClickHandler((e, i) -> {
            settingDisplayItem = !settingDisplayItem;
            setup();
        });

        this.setElement(2, give);
        this.setElement(4, backElement);
        this.setElement(6, editDisplayItem);
        this.setElement(19, rename);
        this.setElement(21, description);
        this.setElement(23, editRewards);
        this.setElement(25, delete);
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}