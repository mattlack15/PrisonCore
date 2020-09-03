package com.soraxus.prisons.bunkers.base.elements.type;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;

public interface BunkerElementConstructor {
    BunkerElement createElement(Bunker bunker, Object... args);
}
