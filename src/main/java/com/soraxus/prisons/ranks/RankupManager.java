package com.soraxus.prisons.ranks;

import java.util.ArrayList;
import java.util.List;

public class RankupManager {
    public static RankupManager instance;

    private ModuleRanks parent;

    /**
     * An ordered list of ranks from lowest to highest
     */
    private List<Rank> ranks = new ArrayList<>(); // TODO: Load from config
    // TODO: Convert from PrisonRanksX config?

    public RankupManager(ModuleRanks parent) {
        this.parent = parent;
        instance = this;
    }
}
