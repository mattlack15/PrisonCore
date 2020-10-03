package com.soraxus.prisons.bunkers.npc.effect;

import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;

@Getter
@AllArgsConstructor
public enum EffectType {
    POISON(BunkerNPC::damage),
    REGEN((b, p) -> b.setHealth(b.getHealth() + p / 2));

    private BiConsumer<BunkerNPC, Integer> update;
}
