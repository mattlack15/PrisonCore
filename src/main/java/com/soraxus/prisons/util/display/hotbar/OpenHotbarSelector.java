package com.soraxus.prisons.util.display.hotbar;

import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.SavedInventory;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Getter
public class OpenHotbarSelector {
    private Player player;
    private SavedInventory state;
    private HotbarSelector selector;

    OpenHotbarSelector(Player player, HotbarSelector selector) {
        this.player = player;
        EventSubscriptions.instance.subscribe(this);
        this.state = new SavedInventory(player.getInventory());
        setSelector(selector);
    }

    public void setSelector(HotbarSelector sel) {
        this.selector = sel;
        player.getInventory().clear();
        for (int i = 0; i < selector.getElements().size(); i ++) {
            SelectableElement e = selector.getElements().get(i);
            if (e == null) {
                continue;
            }
            player.getInventory().setItem(i, e.getItem());
        }
    }

    public void close() {
        HotbarSelectorManager.getInstance().close(this);
        EventSubscriptions.instance.unSubscribe(this);
        state.restore(player.getInventory());
    }

    @EventSubscription
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getWhoClicked().getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventSubscription
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventSubscription
    public void onRightClick(PlayerInteractEvent e) {
        if (!e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
        int slot = e.getPlayer().getInventory().getHeldItemSlot();
        if (slot >= selector.getElements().size()) {
            return;
        }
        SelectableElement el = selector.getElements().get(slot);
        if (el == null) {
            return;
        }
        if (el.getRun().apply(e)) {
            close();
        }
    }
}
