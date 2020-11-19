package com.soraxus.prisons.bunkers.npc.combat;

import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.npc.AbstractBunkerNPCController;
import com.soraxus.prisons.bunkers.npc.AvailableTarget;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.TargetType;
import com.soraxus.prisons.util.list.LockingList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class CombatNPCController extends AbstractBunkerNPCController {
    @Getter
    @Setter
    private Match currentMatch;

    @Getter
    @Setter
    private TargetType targetingType = TargetType.HOSTILE;

    public CombatNPCController(BunkerNPC parent) {
        super(parent);
    }

    private final LockingList<BunkerNPCAbility> abilities = new LockingList<>();

    public List<BunkerNPCAbility> getAbilities() {
        return abilities.getLock().perform(() -> new ArrayList<>(abilities));
    }

    public void addAbility(BunkerNPCAbility ability) {
        this.abilities.add(ability);
    }

    public List<AvailableTarget<?>> getAvailableTargets() {
        if (this.getBunker() == null)
            return new ArrayList<>();
        List<AvailableTarget<?>> targets = new ArrayList<>();
        if (this.getCurrentMatch() != null) {
            targets.addAll(getCurrentMatch().getAttacker().equals(this.getBunker()) ^ targetingType == TargetType.FRIENDLY ?
                    this.getCurrentMatch().getAttackerTargets() :
                    this.getCurrentMatch().getDefenderTargets());
        }
        return targets;
    }

    @Override
    public void tick() {
        super.tick();
        this.getAbilities().forEach(a -> {
            a.tick();
            if (a.getCooldown().get() > 0) {
                a.getCooldown().decrementAndGet();
            }
            if (a.canUse()) {
                if (a.getCooldown().get() <= 0) {
                    a.getCooldown().set(a.cooldownTicks());
                    a.use();
                }
            }
        });
    }
}
