package com.soraxus.prisons.util.display.hotbar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

@Getter
@Setter
@AllArgsConstructor
public class SelectableElement {
    /**
     * Item to be shown in the player's hotbar
     */
    private ItemStack item;

    /**
     * Executor to be run when the item is selected
     * <p>
     * Return <code>true</code> to close the selector
     */
    private Function<PlayerInteractEvent, Boolean> run;
}
