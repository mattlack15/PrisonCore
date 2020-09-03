package com.soraxus.prisons.luckyblocks;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.manager.EnchantManager;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModuleLuckyBlocks extends CoreModule {
    public void runLuckyBlock(Player player) {
        ItemStack pick = player.getInventory().getItemInMainHand();

        AbstractCE favored = EnchantManager.instance.getCE("favored");
        int level = favored.getLevel(pick);

        double rand = MathUtils.random(0, 100D);
        if (rand <= 30.0) { // Money
            long money = (long) Math.floor(1000 * ((level / 25D) + 1));
            Economy.money.addBalance(player.getUniqueId(), money);
            player.sendMessage("§eLucky Blocks > §7You have received §e$" + NumberUtils.formatFull(money) + "§7.");
            return;
        }
        long tokens = (long) Math.floor(5 * ((level / 25D) + 1)); // Large tokens
        if (rand <= 65) { // Small tokens
            tokens = (long) Math.floor(3 * ((level / 25D) + 1));
        }
        player.sendMessage("§eLucky Blocks > §7You have received §e" + NumberUtils.formatFull(tokens) + " Tokens§7.");
        Economy.tokens.addBalance(player.getUniqueId(), tokens);
    }

    @Override
    public String getName() {
        return "Lucky Blocks";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @EventSubscription
    public void onBreak(PrisonBlockBreakEvent e) {
        if ((e.getBlock() & 4095) == Material.SPONGE.getId()) {
            runLuckyBlock(e.getPlayer());
            e.setAmount(0);
        }
    }
}
