package com.soraxus.prisons.bunkers.base.army;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.list.ElementableList;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BunkerArmy {
    private final Bunker parent;
    private final ElementableList<BunkerNPC> availableWarriors;

    public BunkerArmy(Bunker parent, List<BunkerNPC> npcs) {
        this(parent, new ElementableList<>(npcs));
    }

    public int getCount(BunkerNPCType type) {
        return availableWarriors.count(bunkerNPC -> type.equals(bunkerNPC.getType()));
    }
}
