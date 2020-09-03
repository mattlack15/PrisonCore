package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DropResource implements ElementDrop {
    private final BunkerResource resource;
    private final double amount;

    @Override
    public void apply(Bunker bunker) {
        bunker.addResource(resource, amount);
    }
}
