package com.soraxus.prisons.event.bunkers;

import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.matchmaking.MatchTeam;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class MatchNPCSpawnEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final Match match;
    private final MatchTeam team;
    private final BunkerNPC npc;

    public MatchNPCSpawnEvent(Match match, MatchTeam team, BunkerNPC npc) {
        this.match = match;
        this.team = team;
        this.npc = npc;
    }
}
