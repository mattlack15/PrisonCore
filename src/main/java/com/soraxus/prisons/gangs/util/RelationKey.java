package com.soraxus.prisons.gangs.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class RelationKey {
    private UUID gang1;
    private UUID gang2;

    @Override
    public boolean equals(Object o) {
        if (o instanceof RelationKey)
            return ((RelationKey) o).gang1.equals(gang1) && ((RelationKey) o).gang2.equals(gang2) ||
                    ((RelationKey) o).gang2.equals(gang1) && ((RelationKey) o).gang1.equals(gang2);
        return false;
    }
}
