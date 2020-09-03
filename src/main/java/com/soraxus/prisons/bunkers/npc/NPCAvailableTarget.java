package com.soraxus.prisons.bunkers.npc;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class NPCAvailableTarget implements AvailableTarget<BunkerNPC> {
    private BunkerNPC n;

    @Override
    public void apply(Targetter controller) {
        controller.setTarget(n.getController());
    }

    @Override
    public @NotNull BunkerNPC get() {
        return n;
    }

    @Override
    public @NotNull
    Location getImmediateLocation() {
        return n.getController().getLocation().toBukkitVector().toLocation(n.getController().getWorld());
    }

    public boolean exists() {
        return n.getController().getNpc().isSpawned() && n.getController().getNpc().getEntity() != null;
    }
}
