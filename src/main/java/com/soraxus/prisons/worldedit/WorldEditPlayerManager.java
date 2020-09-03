package com.soraxus.prisons.worldedit;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEditPlayerManager {
    public static WorldEditPlayerManager instance;

    private ModuleWorldEdit parent;

    private Map<UUID, WorldEditPlayerState> playerStateMap = new HashMap<>();

    public WorldEditPlayerManager(ModuleWorldEdit parent) {
        instance = this;
        this.parent = parent;
        EventSubscriptions.instance.subscribe(this);
    }

    public WorldEditPlayerState getPlayerState(UUID id) {
        if (!playerStateMap.containsKey(id)) {
            playerStateMap.put(id, new WorldEditPlayerState(id));
        }
        return playerStateMap.get(id);
    }

    public ItemStack getWand() {
        return new ItemBuilder(Material.WOOD_SPADE).build();
    }

    public boolean isWand(ItemStack item) {
        return item != null && item.getType() == Material.WOOD_SPADE;
    }

    @EventSubscription
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("asyncworld.worldedit")) {
            return;
        }
        if (!isWand(e.getItem())) {
            return;
        }
        e.setCancelled(true);
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            getPlayerState(e.getPlayer().getUniqueId()).setPos1(e.getClickedBlock().getLocation());
            e.getPlayer().sendMessage(SpigotPrisonCore.PREFIX + "§aPosition 1 set");
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            getPlayerState(e.getPlayer().getUniqueId()).setPos2(e.getClickedBlock().getLocation());
            e.getPlayer().sendMessage(SpigotPrisonCore.PREFIX + "§aPosition 2 set");
        }
    }
}
