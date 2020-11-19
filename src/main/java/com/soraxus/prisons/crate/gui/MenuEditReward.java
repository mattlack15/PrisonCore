package com.soraxus.prisons.crate.gui;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.crate.Reward;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
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

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuEditReward extends Menu {
    private Crate crate;
    private Reward reward;
    private MenuElement backElement;
    private UUID chatConsumerPlayer = null;
    private Consumer<String> chatConsumer = null;
    private boolean gettingDisplayItem = false;

    public MenuEditReward(Crate crate, Reward reward, MenuElement element) {
        super("Edit Reward", 5);
        EventSubscriptions.instance.subscribe(this);
        this.crate = crate;
        this.reward = reward;
        this.backElement = element;
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
                    if (gettingDisplayItem) {
                        reward.setDisplayItem(new ItemStack(stack.getType(), 1, (short) 0, stack.getData().getData()));
                        gettingDisplayItem = false;
                    } else {
                        reward.getRewardItems().add(stack);
                    }
                    CrateManager.instance.saveCrate(crate);
                    this.setup();
                }
            }
        }
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

    public void setup() {
        MenuElement editDescription = new MenuElement(new ItemBuilder(Material.NAME_TAG, 1).setName("&a&lDescription")
                .addLore("&fCurrently: " + reward.getDescription()).addLore("").addLore("&8Click to Change").build()).setClickHandler((e, i) -> {
            e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Type the description in chat");
            chatConsumer = (s) -> {
                reward.setDescription(s);
                CrateManager.instance.saveCrate(crate);
                setup();
                Synchronizer.synchronize(() -> open((Player) e.getWhoClicked()));
                MenuCrates.noGC.remove(this);
            };
            MenuCrates.noGC.add(this);
            this.chatConsumerPlayer = e.getWhoClicked().getUniqueId();
            e.getWhoClicked().closeInventory();
        });

        List<String> commands = reward.getCommands();

        this.setupActionableList(19, 24, 18, 25, (index) -> {
            if (index >= commands.size())
                return null;

            String command = commands.get(index);
            return new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 5).setName("&a&l/" + command).addLore("&7Type: &fCommand")
                    .addLore("").addLore("&cRight Click to Delete").build())
                    .setClickHandler((e, i) -> {
                        if (e.getClick().isRightClick()) {
                            reward.getCommands().remove(command);
                            CrateManager.instance.saveCrate(crate);
                            this.setup();
                        }
                    });
        }, 0);

        MenuElement addCommand = new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&a&lAdd Command").addLore("&7Click to add a command").build())
                .setClickHandler((e, i) -> {
                    e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Type the command in chat without a / ex. \"give <player> diamond 1\"");
                    e.getWhoClicked().sendMessage(ChatColor.GRAY + "You may use <player>");
                    chatConsumer = (s) -> {
                        reward.getCommands().add(s);
                        CrateManager.instance.saveCrate(crate);
                        setup();
                        Synchronizer.synchronize(() -> open(e.getWhoClicked()));
                        MenuCrates.noGC.remove(this);
                    };
                    MenuCrates.noGC.add(this);
                    this.chatConsumerPlayer = e.getWhoClicked().getUniqueId();
                    e.getWhoClicked().closeInventory();
                });

        MenuElement addItem = new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&a&lAdd Item").addLore("&7Click an displayItem in your inventory").build());

        List<ItemStack> stacks = reward.getRewardItems();

        this.setupActionableList(28, 33, 27, 34, (index) -> {
            if (index >= stacks.size())
                return null;

            ItemStack stack = stacks.get(index);

            return new MenuElement(new ItemBuilder(stack).addLore("").addLore("&7Type: &fItem")
                    .addLore("").addLore("&cRight Click to Delete").build())
                    .setClickHandler((e, i) -> {
                        if (e.getClick().isRightClick()) {
                            reward.getRewardItems().remove(stack);
                            CrateManager.instance.saveCrate(crate);
                            this.setup();
                        }
                    });
        }, 0);

        ItemBuilder builder;
        if (gettingDisplayItem) {
            builder = new ItemBuilder(Material.GLASS_BOTTLE, 1).setName("&cWaiting...").addLore("&fClick on an item in your inventory");
        } else {
            builder = new ItemBuilder(reward.getDisplayItem()).addLore("").addLore("&fClick to Change");
        }

        MenuElement editDisplayItem = new MenuElement(builder.build()).setClickHandler((e, i) -> {
            gettingDisplayItem = !gettingDisplayItem;
            setup();
        });

        this.setElement(4, backElement);
        this.setElement(2, editDisplayItem);
        this.setElement(26, addCommand);
        this.setElement(35, addItem);
        this.setElement(6, editDescription);
        MenuManager.instance.invalidateInvsForMenu(this);
    }

}
