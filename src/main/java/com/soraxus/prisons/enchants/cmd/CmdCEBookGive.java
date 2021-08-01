package com.soraxus.prisons.enchants.cmd;

import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.manager.EnchantManager;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.IntegerProvider;
import net.ultragrav.command.provider.impl.StringProvider;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdCEBookGive extends SpigotCommand {
    public CmdCEBookGive() {
        addAlias("give");
        addParameter(PlayerProvider.getInstance(), "player");
        addParameter(StringProvider.getInstance(), "enchantment");
        addParameter(IntegerProvider.getInstance(), "level");
        addParameter(1, IntegerProvider.getInstance(), "amount");
    }

    @Override
    protected void perform() {
        Player player = getArgument(0);
        String enchantName = getArgument(1);
        int level = getArgument(2);
        int amount = getArgument(3);

        AbstractCE ce = EnchantManager.instance.getCE(enchantName.replace("_", " "));

        if (ce == null) {
            tell("&cThat is not an enchantment");
            StringBuilder builder = new StringBuilder();
            EnchantManager.instance.getEnchantments().forEach(e -> {
                builder.append(e.getName().replace(" ", "_"));
                builder.append(", ");
            });

            if (builder.length() != 0)
                for (int i = 0; i < 2; i++)
                    builder.deleteCharAt(builder.length() - 1);

            tell("&cValid enchantments are: " + builder.toString() + ".");
            return;
        }

        ItemStack stack = ce.getBook(level);
        stack.setAmount(amount);

        player.getInventory().addItem(stack);
        tell("&aThey have been given the requested book!");
    }
}
