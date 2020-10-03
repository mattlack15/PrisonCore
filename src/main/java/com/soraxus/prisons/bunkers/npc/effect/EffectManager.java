package com.soraxus.prisons.bunkers.npc.effect;

import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.util.list.ElementableList;

import java.util.ArrayList;
import java.util.List;

public class EffectManager {
    private BunkerNPC parent;

    private ElementableList<NPCEffect> effectList = new ElementableList<>();

    public EffectManager(BunkerNPC parent) {
        this.parent = parent;
    }

    public void update() {
        List<NPCEffect> toRemove = new ArrayList<>();
        for (NPCEffect effect : effectList) {
            if (System.currentTimeMillis() > effect.getStartTime() + effect.getDuration()) {
                toRemove.add(effect);
                continue;
            }
            effect.getType().getUpdate().accept(parent, effect.getPotency());
        }
        effectList.removeAll(toRemove);
    }

    public void addEffect(NPCEffect e) {
        NPCEffect oldEff = effectList.byFunction(e.getType(), NPCEffect::getType);
        if (oldEff != null) {
            if (oldEff.getPotency() >= e.getPotency()) {
                return;
            }
            effectList.remove(oldEff);
        }
        effectList.add(e);
    }
}
