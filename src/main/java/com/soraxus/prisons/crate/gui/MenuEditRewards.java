package com.soraxus.prisons.crate.gui;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.crate.Reward;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.InvInfo;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuEditRewards extends Menu {

    private Crate crate;
    private MenuElement backElement;

    private Consumer<String> chatConsumer = null;
    private UUID chatConsumerPlayer = null;

    public MenuEditRewards(Crate crate, MenuElement backElement) {
        super("Rewards of " + crate.getDisplayName(), 5);
        EventSubscriptions.instance.subscribe(this);
        this.crate = crate;
        this.backElement = backElement;
        this.setup();
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
                    event.setCancelled(true);
                    crate.getRewards().add(new Reward("&b4x No Description", new ArrayList<>(), new ArrayList<>(), new ItemStack(stack.getType(), 1, (short) 0, stack.getData().getData()), 0.5));
                    CrateManager.instance.saveCrate(crate);
                    this.setup();
                }
            }
        }
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        if(chatConsumer != null && event.getPlayer().getUniqueId().equals(chatConsumerPlayer)) {
            event.setCancelled(true);
            chatConsumer.accept(event.getMessage());
            chatConsumer = null;
            chatConsumerPlayer = null;
        }
    }

    public void setup() {
        this.setAll(null);
        List<Reward> rewards = new ArrayList<>(crate.getRewards());

        this.setElement(6, new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&a&lAdd")
                .addLore("&fClick to add a Reward").addLore("&fOr click on an item in your inventory")
                .build()).setClickHandler((e, i) -> {
                Reward reward = new Reward("&b4x &eNo description", new ArrayList<>(), new ArrayList<>(), new ItemBuilder(Material.PAPER, 1).setName("&aReward").build(), 0.5);
                crate.getRewards().add(reward);
                setup();
        }));

        this.setElement(4, backElement);

        this.setupActionableList(10, 9 * 4 - 2, 9 * 4, 9 * 5 - 1, (index) -> {
            if (index >= rewards.size())
                return null;
            Reward reward = rewards.get(index);
            ItemBuilder builder = new ItemBuilder(reward.getDisplayItem());
            builder.setName(reward.getDescription());
            builder.addLore("&fItems: &7" + reward.getRewardItems().size());
            builder.addLore("&fCommands: ");
            for(String cmd : reward.getCommands())
                builder.addLore("&7 - " + cmd);
            builder.addLore("");
            builder.addLore("&fChance: &a" + (reward.getChance() * 100d) + "%");
            builder.addLore("");
            builder.addLore("&aClick to Edit");
            builder.addLore("&bShift-Left Click to set chance");
            builder.addLore("&cShift-Right Click to Remove");
            return new MenuElement(builder.build()).setClickHandler((e, i) -> {
                if(e.getClick().isShiftClick() && e.getClick().isRightClick()) {
                    crate.getRewards().remove(reward);
                    CrateManager.instance.saveCrate(crate);
                    this.setup();
                } else if(e.getClick().isShiftClick() && e.getClick().isLeftClick()) {
                    this.chatConsumer = (s) -> {
                        try {
                            double chance = Double.parseDouble(s);
                            reward.setChance(chance / 100d);
                        } catch(Exception ignored) {}
                        this.setup();
                        this.open((Player)e.getWhoClicked());
                        MenuCrates.noGC.remove(this);
                    };
                    MenuCrates.noGC.add(this);
                    this.chatConsumerPlayer = e.getWhoClicked().getUniqueId();
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter the chance in chat");
                } else {
                    new MenuEditReward(crate, reward, getBackButton((e1, i1) -> {
                        setup();
                        open((Player)e1.getWhoClicked());
                    })).open((Player)e.getWhoClicked());
                }
            });
        }, 0);
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
