package com.soraxus.prisons.bunkers.matchmaking;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.util.list.ElementableLockingList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


public class BunkerMatchMaker {
    public static final double RATING_GRACE_PERIOD = 2D;
    public static final double MATCHING_TOLERANCE = 10D; //TODO: change to 2.4
    public static BunkerMatchMaker instance;

    private final BunkerManager manager;

    private final ElementableLockingList<Match> matchList = new ElementableLockingList<>();

    public BunkerMatchMaker(BunkerManager manager) {
        instance = this;
        this.manager = manager;
    }

    public boolean matchExists(Bunker attacker, Bunker defender) {
        return matchList.contains(new Match(attacker, defender, this));
    }

    public List<Match> getReservedMatches() {
        return matchList.copy();
    }

    public Match getMatchFromAttacker(Bunker attacker) {
        return matchList.byFunction(attacker, Match::getAttacker);
    }

    public Match getMatchFromDefender(Bunker defender) {
        return matchList.byFunction(defender, Match::getDefender);
    }

    public Match reserveMatch(@NotNull Bunker attacker, @NotNull Bunker defender) {
        Match match = new Match(attacker, defender, this);
        return matchList.getLock().perform(() -> {
            if (matchList.contains(match))
                return null;
            matchList.add(match);
            return match;
        });
    }

    public void freeMatch(Match match) {
        matchList.remove(match);
    }

    public List<UUID> findMatches(double rating) {
        Map<UUID, Double> ratings = BunkerManager.instance.getCachedRatings().copy();

        return ratings.entrySet().stream()
                .filter((e) -> Math.abs(rating - e.getValue()) <= MATCHING_TOLERANCE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void tick() {
        this.matchList.forEach(Match::tick);
    }
}
