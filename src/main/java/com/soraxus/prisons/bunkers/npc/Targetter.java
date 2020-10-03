package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.BunkerElement;
import lombok.Getter;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Just for cleanness
 */
public class Targetter {
    @Getter
    private final AbstractBunkerNPCController parent;

    public Targetter(AbstractBunkerNPCController controller) {
        this.parent = controller;
    }

    /**
     * Set target to a location
     */
    public void setTarget(Vector3D target) {
        NPC npc = parent.getNpc();
        NavigatorParameters params = npc.getNavigator().getLocalParameters();
        params.range((float) npc.getEntity().getLocation().toVector().distance(target.toBukkitVector()) + 5);
        params.stuckAction((npc1, navigator) -> {
            Bukkit.broadcastMessage("I've fallen and I can't get up");
            npc1.teleport(npc1.getEntity().getLocation().add(0, 1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
            return true;
        });
        //params.useNewPathfinder(true);
        npc.getNavigator()
                .setTarget(target.toBukkitVector().toLocation(parent.getWorld()));
        npc.getNavigator().setPaused(false);
    }

    public void setTarget(BunkerElement element) {
        setTarget(element.getBoundingRegion(1.8D * 2D).getCenter());
    }

    public void setTarget(AbstractBunkerNPCController other) {
        NPC npc = parent.getNpc();
        if(!other.isSpawned())
            return;
        NavigatorParameters params = npc.getNavigator().getLocalParameters();
        params.range((float) npc.getEntity().getLocation().toVector().distance(other.getNpc().getEntity().getLocation().toVector()) + 5);
        params.stuckAction((npc1, navigator) -> {
            Bukkit.broadcastMessage("I've fallen and I can't get up");
            npc1.teleport(npc1.getEntity().getLocation().add(0, 1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
            return true;
        });
        //params.useNewPathfinder(true);
        params.speedModifier(2F);
        npc.getDefaultGoalController().clear();
        npc.getDefaultGoalController().setPaused(true);
        npc.getNavigator().setTarget(other.getNpc().getEntity(), false);
        npc.getNavigator().setPaused(false);
        Bukkit.broadcastMessage("Set target to entity");
    }
}
