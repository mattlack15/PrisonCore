package com.soraxus.prisons.bunkers.npc.effect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NPCEffect {
    private final EffectType type;
    private final int potency;
    private final long startTime = System.currentTimeMillis();
    private final long duration;
}
