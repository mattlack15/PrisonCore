package com.soraxus.prisons.bunkers.base.elements.defense.active;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.event.bunkers.BunkerMatchEndEvent;
import com.soraxus.prisons.event.bunkers.BunkerMatchStartEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.serializer.GravSerializer;

public abstract class ActiveDefenseElement extends BunkerElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ActiveDefenseElement(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
        EventSubscriptions.instance.subscribe(this, ActiveDefenseElement.class);
    }

    protected void onDefendingMatchStart() {}
    protected void onDefendingMatchEnd() {}
    public boolean isDefendingMatchActive() {
        return getBunker().getDefendingMatch() != null;
    }

    @EventSubscription
    private void onMatchStart(BunkerMatchStartEvent event) {
        if(event.getMatch().getDefender() == getBunker()) {
            onDefendingMatchStart();
        }
    }

    @EventSubscription
    private void onMatchEnd(BunkerMatchEndEvent event) {
        if(event.getMatch().getDefender() == getBunker()) {
            onDefendingMatchEnd();
        }
    }
}
