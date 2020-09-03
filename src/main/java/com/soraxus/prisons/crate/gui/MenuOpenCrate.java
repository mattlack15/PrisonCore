package com.soraxus.prisons.crate.gui;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.Reward;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class MenuOpenCrate extends Menu {
    private Crate crate;
    private List<Reward> its = new ArrayList<>();
    private int counter = 0;
    private int itt = 0;

    public MenuOpenCrate(Crate crate) {
        super("Opening Crate", 3);
        this.crate = crate;
        redraw();
    }

    private int getShrinkItt() {
        return Math.floorDiv(itt, 10);
    }

    private int getDesiredSize() {
        return (7 - 2 * getShrinkItt());
    }

    public void populate() {
        int desiredSize = getDesiredSize();
        while (its.size() > desiredSize) {
            its.remove(0);
        }
        while (its.size() < desiredSize) {
            its.add(crate.getReward());
        }
    }

    public boolean iterate() {
        if (getDesiredSize() == 1) {
            populate();
            return false; // Cannot iterate anymore
        }
        counter ++;
        if (counter > getShrinkItt()) {
            counter = 0;
            itt++;
            its.remove(0);
            redraw();
        }
        return true;
    }

    public Reward getFinalReward() {
        return its.get(0);
    }

    public void redraw() { // Could be made more efficient, this is to make it simple for now
        populate();
        clear();

        MenuElement border = new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1)
                .setDurability((short) 15)
                .setName("§7")
                .build()
        ).setStaticItem(true);
        setRow(0, border);
        setRow(2, border);

        int shrinkItt = getShrinkItt();
        for (int i = 0; i < 7 - 2 * shrinkItt; i++) {
            setElement(10 + shrinkItt + i, new MenuElement(its.get(i).getDisplayItem()).setStaticItem(true));
        }

        MenuElement border2 = new MenuElement(
                new ItemBuilder(Material.STAINED_GLASS_PANE, 1)
                        .setDurability((short) 5)
                        .setName("§7")
                        .build()
        ).setStaticItem(true);
        fillElement(border2);
    }
}