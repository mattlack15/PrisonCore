package com.soraxus.prisons.luckyblocks;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.enchants.customenchants.Favored;
import com.soraxus.prisons.enchants.gui.MenuEnchant;
import com.soraxus.prisons.enchants.manager.EnchantManager;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModuleLuckyBlocks extends CoreModule {
    public void runLuckyBlock(Player player) {
        ItemStack pick = player.getInventory().getItemInMainHand();

        AbstractCE favored = EnchantManager.instance.getCE(Favored.class);
        int level = favored.getLevel(pick);

        double rand = MathUtils.random(0, 100D);
        if (rand <= 30.0) { // Money
            long money = (long) Math.floor(1000 * ((level / 25D) + 1));
            Economy.money.addBalance(player.getUniqueId(), money);
            player.sendMessage("§eLucky Blocks > §7You have received §e$" + NumberUtils.formatFull(money) + "§7.");
            return;
        }
        long tokens = favored.getCost(level) / 50;
        if (rand <= 65) { // Small tokens
            tokens = favored.getCost(level) / 60;
        }
        ChatBuilder builder = new ChatBuilder("§eLucky Blocks > §7You have received §e" + NumberUtils.formatFull(tokens) + " Tokens§7.");
        if (tokens == 0) {
            builder.addText(" &7You need\nthe " + EnchantManager.instance.getCE(Favored.class).getDisplayName() + " &7enchant to get lucky block rewards!",
                    HoverUtil.text("&fClick to open&d enchant menu"),
                    ClickUtil.runnable((p) -> {
                        if (p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType().toString().contains("PICKAXE")) {
                            new MenuEnchant(p, p.getInventory().getItemInMainHand(), EnchantManager.instance.getEnchantments()).open(p);
                        } else {
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.8f, 1.2f);
                            new ChatBuilder("&cYou must be holding your pickaxe to open the enchantment menu!").send(p);
                        }
                    }));
        }
        builder.send(player);
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
            for (int i = 0; i < e.getAmount(); i++)
                runLuckyBlock(e.getPlayer());
            e.setAmount(0);
        }
    }
}
