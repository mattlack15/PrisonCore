package com.soraxus.prisons.bunkers.base.army;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class BunkerArmy {
    private final Bunker parent;
    private final List<BunkerNPC> availableWarriors;

    public int getCount(BunkerNPCType type) {
        int i = 0;
        for (BunkerNPC availableWarrior : availableWarriors) {
            if(availableWarrior != null && availableWarrior.getType() == type) {
                i++;
            }
        }
        return i;
    }
}
