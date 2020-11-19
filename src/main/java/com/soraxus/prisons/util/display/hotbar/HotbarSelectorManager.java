package com.soraxus.prisons.util.display.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HotbarSelectorManager {
    @Getter
    private static HotbarSelectorManager instance = new HotbarSelectorManager();

    private List<OpenHotbarSelector> openSelectors;

    private HotbarSelectorManager() {
        this.openSelectors = new ArrayList<>();
    }

    /**
     * Get the current open HotbarSelector of a player
     *
     * @param player Player
     * @return OpenHotbarSelector
     */
    public OpenHotbarSelector getSelector(Player player) {
        for (OpenHotbarSelector selector : openSelectors) {
            if (selector.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return selector;
            }
        }
        return null;
    }

    /**
     * Open a hotbar selector for a player
     *
     * @param player   Player
     * @param selector HotbarSelector
     * @return Previous open HotbarSelector of player
     */
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

    /**
     * Close an open HotbarSelector
     *
     * @param sel OpenHotbarSelector
     * @return HotbarSelector
     */
    public HotbarSelector close(OpenHotbarSelector sel) {
        openSelectors.remove(sel);
        return sel.getSelector();
    }
}
