package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.NumberUtils;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.LongProvider;
import org.bukkit.inventory.ItemStack;

public class CmdVoucher extends SpigotCommand {
    public CmdVoucher() {
        this.addAlias("voucher");
        this.addAlias("vouchers");
        this.setAllowConsole(false);
        this.setHelpHeader(null);
        this.setHelpFooter("&aValid Types: &7money, tokens, stars");
        this.setHelpFormat("&f/&7<cmd> <args>");
        this.addParameter(EconomyProvider.getInstance(), "economy type");
        this.addParameter(LongProvider.getInstance(), "amount");
    }

    @Override
    protected void perform() {
        Economy eco = getArgument(0);

        if (!eco.hasBalance(getPlayer().getUniqueId(), getArgument(1))) {
            tell(CmdEco.PREFIX + "&cYou don't have enough of the specified currency!");
            return;
        }

        ItemStack voucher = eco.createNote(getArgument(1));
        for (ItemStack contents : getSpigotPlayer().getInventory().getStorageContents()) {
            if (contents == null) {
                getSpigotPlayer().getInventory().addItem(voucher);
                eco.removeBalance(getPlayer().getUniqueId(), getArgument(1));
                tell(CmdEco.PREFIX + "&7Created a voucher for &a$" + NumberUtils.toReadableNumber(getArgument(1)));
                return;
            }
        }
        tell(CmdEco.PREFIX + "&cYou don't have enough space in your inventory!");
    }
}
