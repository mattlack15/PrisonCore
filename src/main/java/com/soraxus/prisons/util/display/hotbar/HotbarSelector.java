package com.soraxus.prisons.util.display.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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

    public HotbarSelector(Function<Integer, SelectableElement> generator) {
        this.elements = new ArrayList<>();
        for (int i = 0; i < 9; i ++) {
            elements.add(generator.apply(i));
        }
    }

    public void setElement(int slot, SelectableElement e) {
        if (slot < 0 || slot >= 9) {
            throw new IllegalArgumentException("slot must be in range [0,9)");
        }
        elements.set(slot, e);
    }

    public HotbarSelector open(Player player) {
        return HotbarSelectorManager.getInstance().open(player, this);
    }
}
