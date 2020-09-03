package com.soraxus.prisons.util.display.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@Getter
public class HotbarSelector {
    private List<SelectableElement> elements;

    public HotbarSelector(List<SelectableElement> elements) {
        if (elements.size() > 9) {
            throw new IllegalArgumentException("Elements must be of size 9 or lower");
        }
        this.elements = elements;
    }

    public HotbarSelector(SelectableElement... elements) {
        this(Arrays.asList(elements));
    }

    public HotbarSelector open(Player player) {
        return HotbarSelectorManager.getInstance().open(player, this);
    }
}
