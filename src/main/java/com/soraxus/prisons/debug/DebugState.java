package com.soraxus.prisons.debug;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class DebugState {
    private final UUID id;
    private boolean debug_all = false;
    private List<String> debugged = new ArrayList<>();
}
