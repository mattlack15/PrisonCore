package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.npc.combat.CombatNPCController;
import com.soraxus.prisons.bunkers.npc.effect.EffectManager;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.Vector3D;
import org.bukkit.Location;

@Getter
public class BunkerNPC implements GravSerializable {
    private final BunkerNPCType type;
    private AbstractBunkerNPCController controller = null;
    private int level;
    private double maxHealth;
    @Setter
    private double health;
    private EffectManager effectManager;

    public BunkerNPC(BunkerNPCType type, int level) {
        this.level = level;
        this.type = type;
        this.maxHealth = type.getInfo().getMaxHealth(level);
        this.health = maxHealth;
        this.effectManager = new EffectManager(this);
    }

    public static BunkerNPC deserialize(GravSerializer serializer) {
        return new BunkerNPC(serializer.readObject(), serializer.readInt());
    }

    public void tick() {
        this.effectManager.update();
    }

    public void spawn(Bunker homeBunker, NPCManager manager, Location location) {
        if (this.controller != null) {
            this.controller.remove();
        }
        controller = type.getController(this);
        controller.spawnNPC(homeBunker, manager, location.getWorld(), Vector3D.fromBukkitVector(location.toVector()));
    }

    public void setMatch(Match match) {
        if (!(this.controller instanceof CombatNPCController)) {
            throw new IllegalStateException("This NPC is not a combat NPC so it does not have a match");
        }
        ((CombatNPCController) this.controller).setCurrentMatch(match);
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeObject(type);
        gravSerializer.writeInt(level);
    }

    public void damage(double i) {
        this.health -= i;
        if (this.health < 0) {
            getController().remove();
        }
    }

    public void heal(double i) {
        this.health += i;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
    }
}
