package com.soraxus.prisons.util.display.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HotbarSelectorManager {
    @Getter
    private static HotbarSelectorManager instance = new HotbarSelectorManager();

    private List<OpenHotbarSelector> openSelectors;

    public HotbarSelectorManager() {
        this.openSelectors = new ArrayList<>();
    }

    public OpenHotbarSelector getSelector(Player player) {
        for (OpenHotbarSelector selector : openSelectors) {
            if (selector.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return selector;
            }
        }
        return null;
    }

    public HotbarSelector open(Player player, HotbarSelector selector) {
        OpenHotbarSelector sel = getSelector(player);
        if (sel == null) {
            this.openSelectors.add(new OpenHotbarSelector(player, selector));
            return null;
        }
        HotbarSelector old = sel.getSelector();
        sel.setSelector(selector);
        return old;
    }

    public HotbarSelector close(OpenHotbarSelector sel) {
        openSelectors.remove(sel);
        return sel.getSelector();
    }
}
