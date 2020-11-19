package com.soraxus.prisons.util.metadata;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.util.EventSubscriptions;
import org.bukkit.metadata.FixedMetadataValue;

public abstract class EventMetadata extends FixedMetadataValue {
    public EventMetadata() {
        super(SpigotPrisonCore.instance, null);
        EventSubscriptions.instance.subscribe(this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        EventSubscriptions.instance.unSubscribe(this);
    }
}
