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
    private ItemStack item;
    private Function<PlayerInteractEvent, Boolean> run;
}
