package com.soraxus.prisons.economy;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.economy.command.CmdEco;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ModuleEconomy extends CoreModule {
    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.DOUBLE_PLANT).setName("&f&lEconomies").addLore("&7Click to view &f&lEconomies")
                .build()).setClickHandler((e, i) -> new MenuModuleEconomy(backButton).open(e.getWhoClicked()));
    }

    @EventSubscription
    private void onClick(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR))
            return;
        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        for (Map.Entry<String, Economy> entry : Economy.economies.entrySet()) {
            if (entry.getValue().isValidNote(stack)) {
                long amount = entry.getValue().getNoteValue(stack);
                entry.getValue().addBalance(event.getPlayer().getUniqueId(), amount);
                if (stack.getAmount() <= 1) {
                    event.getPlayer().getInventory().setItemInMainHand(null);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                event.getPlayer().spigot().sendMessage(new ChatBuilder().addText(CmdEco.PREFIX + "You have redeemed a bank note for &e" +
                        entry.getValue().getFormat().replace("%s", NumberUtils.toReadableNumber(amount))).build());
                event.setCancelled(true);
                return;
            }
        }
    }
}
