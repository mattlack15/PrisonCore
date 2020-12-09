package com.soraxus.prisons.gangs.cmd.admin;

import lombok.AllArgsConstructor;

import java.util.function.BiFunction;

@AllArgsConstructor
public enum XpOperation {
    ADD(Long::sum),
    REMOVE((l, l2) -> l - l2),
    SET((l, l2) -> l2);

    private BiFunction<Long, Long, Long> convert;

    public long apply(long l, long l2) {
        return convert.apply(l, l2);
    }
}
