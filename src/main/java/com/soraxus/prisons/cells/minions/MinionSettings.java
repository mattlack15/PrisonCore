package com.soraxus.prisons.cells.minions;

import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

@Getter
@Setter
public class MinionSettings implements GravSerializable {
    private String skullName = "omega_warrior"; // TODO: Change default skull

    public MinionSettings() {}

    public MinionSettings(GravSerializer serializer) {
        Meta stored = serializer.readObject();
        this.skullName = stored.getOrSet("skullName", "omega_warrior");

    }

    public void serialize(GravSerializer serializer) {
        Meta store = new Meta();
        store.set("skullName", this.skullName);

        serializer.writeObject(store);
    }
}
