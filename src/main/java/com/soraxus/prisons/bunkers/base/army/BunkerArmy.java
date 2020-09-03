package com.soraxus.prisons.bunkers.base.army;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class BunkerArmy {
    private Bunker parent;
    private List<BunkerNPC> availableWarriors;
}
