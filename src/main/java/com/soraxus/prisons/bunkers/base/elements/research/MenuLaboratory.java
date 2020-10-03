package com.soraxus.prisons.bunkers.base.elements.research;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.DateUtils;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;

import java.util.concurrent.atomic.AtomicInteger;

public class MenuLaboratory extends Menu {

    private final ElementLaboratory laboratory;

    public MenuLaboratory(ElementLaboratory laboratory) {
        super("Laboratory", 5);
        this.laboratory = laboratory;
        this.setup();
    }

    public void setup() {
        this.setAll(null);
        AtomicInteger index1 = new AtomicInteger();
        this.setupActionableList(10, 35, 36, 44, (index) -> {
            BunkerNPCType type = null;
            while(index1.get() < BunkerNPCType.values().length) {
                index = index1.getAndIncrement();
                type = BunkerNPCType.values()[index];
                if (type.getRequiredBarracksLevel() <= laboratory.getLevel())
                    break;
            }
            if(type == null)
                return null;
            ItemBuilder builder = new ItemBuilder(type.getDisplayItem());
            int currentLevel = laboratory.getBunker().getNpcSkillManager().getLevel(type);
            Storage[] cost = type.getUpgradeCost(currentLevel);
            int time = type.getUpgradeTime(currentLevel);

            builder.addLore("&8Level: &f" + currentLevel);

            if(currentLevel + 1 > type.getInfo().getMaxLevel()) {
                builder.addLore("",
                        "&cMax level reached");
            } else {

                builder.addLore("");
                builder.addLore(type.getInfo().getUpgradePerks(currentLevel));

                if (laboratory.getBunker().hasResources(cost)) {
                    builder.addLore("",
                            "&a&lUpgrade",
                            " &9" + DateUtils.readableDate(time / 20, true));
                    for (Storage storage : cost) {
                        builder.addLore(" &7" + storage.getResource().fullDisplay() + " &f" + storage.getAmount());
                    }
                } else {
                    builder.addLore("",
                            "&cCannot Upgrade (too poor)");
                }
            }

            BunkerNPCType finalType = type;
            return new MenuElement(builder.build()).setClickHandler((e, i) -> {
                int currentLevelNow = laboratory.getBunker().getNpcSkillManager().getLevel(finalType);
                Storage[] costNow = finalType.getUpgradeCost(currentLevelNow);
                if(laboratory.getBunker().hasResources(costNow) && currentLevelNow + 1 <= finalType.getInfo().getMaxLevel()) {
                    laboratory.getBunker().removeResources(costNow);
                    laboratory.getBunker().getNpcSkillManager().setLevel(finalType, currentLevelNow + 1);
                }
                this.setup();
            });
        }, 0);


        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
