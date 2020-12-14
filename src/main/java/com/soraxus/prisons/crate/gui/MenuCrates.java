package com.soraxus.prisons.crate.gui;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import com.soraxus.prisons.util.string.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuCrates extends Menu {
    private MenuElement backElement;

    public static List<Menu> noGC = new ArrayList<>();

    public MenuCrates(MenuElement backElement) {
        super("Crates", 5);
        this.backElement = backElement;
        EventSubscriptions.instance.subscribe(this);
        this.setup();
    }

    private Consumer<String> chatConsumer = null;
    private UUID chatConsumerPlayer = null;

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
        this.setElement(4, backElement);
        List<Crate> crates = CrateManager.instance.getLoaded();
        this.setupActionableList(10, 9 * 4 - 1, 9 * 4, 9 * 5 - 1, (index) -> {
            if (index >= crates.size()) {
                return null;
            }

            Crate crate = crates.get(index);

            Material material = crate.getItemMaterial();
            byte data = crate.getItemData();

            ItemBuilder builder = new ItemBuilder(material, 1, data);
            builder.setName("&6&l" + crate.getName());
            builder.addLore("&8Display Name: &f" + crate.getDisplayName());
            builder.addLore("&8Reward Count: &f" + crate.getRewards().size());
            builder.addLore("&8Description:");
            builder.addLore("");
            for (String descLine : TextUtil.splitIntoLines(crate.getDescription(), 30)) {
                builder.addLore("&7" + descLine);
            }
            builder.addLore("");
            builder.addLore("&fShift-Left Click &7-> Retrieve a Crate");
            builder.addLore("&fShift-Right Click &7-> Retrieve a stack of Crates");
            builder.addLore("&fLeft Click &7to Edit");

            return new MenuElement(builder.build()).setClickHandler((e, i) -> {
                if (e.getClick().isLeftClick() && e.getClick().isShiftClick()) {
                    ItemStack stack = crate.getItem();
                    stack.setAmount(1);
                    e.getWhoClicked().getInventory().addItem(stack);
                } else if (e.getClick().isRightClick() && e.getClick().isShiftClick()) {
                    ItemStack stack = crate.getItem();
                    stack.setAmount(64);
                    e.getWhoClicked().getInventory().addItem(stack);
                } else {
                    new MenuEditCrate(crate, getBackButton((e1, i1) -> {
                        this.setup();
                        open((Player) e1.getWhoClicked());
                    })).open((Player) e.getWhoClicked());
                }
            });
        }, 0);
        MenuElement addCrate = new MenuElement(new ItemBuilder(Material.ANVIL, 1).setName("&a&lAdd").build()).setClickHandler((e, i) -> {
            chatConsumer = (s) -> {
                Crate crate = new Crate(s, s, "&fNo Description :(", new ArrayList<>(), Material.CHEST, (byte) 0, 0);
                CrateManager.instance.add(crate);
                setup();
                Synchronizer.synchronize(() -> open(e.getWhoClicked()));
                noGC.remove(this);
            };
            this.chatConsumerPlayer = e.getWhoClicked().getUniqueId();
            e.getWhoClicked().closeInventory();
            e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter the name in chat");
        });
        this.setElement(6, addCrate);
        noGC.add(this);
        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
