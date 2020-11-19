package com.soraxus.prisons.debug;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DebugManager {
    @Getter
    private static DebugManager instance;

    private ModuleDebug parent;
    private Map<UUID, DebugState> data = new HashMap<>();

    public DebugManager(ModuleDebug parent) {
        this.parent = parent;
        instance = this;
    }

    public DebugState getState(UUID id) {
        if (!data.containsKey(id)) {
            data.put(id, new DebugState(id));
        }
        return data.get(id);
    }
}
