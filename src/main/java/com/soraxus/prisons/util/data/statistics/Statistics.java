package com.soraxus.prisons.util.data.statistics;

import com.soraxus.prisons.util.list.ElementableLockingList;

import java.util.UUID;

public class Statistics {
    private UUID id;
    private ElementableLockingList<Statistic> statistics;

    public Statistics(UUID id) {
        this.id = id;
        this.statistics = new ElementableLockingList<>();
    }

    public Statistic getStatistic(String name) {
        return statistics.byFunctionOrAdd(name, Statistic::getName, new Statistic(name));
    }
}
