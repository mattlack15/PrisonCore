package com.soraxus.prisons.privatemines;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

@Getter
@AllArgsConstructor
@Setter
public class CachedMineInfo implements GravSerializable {
    private String gangName;
    private long rentPrice;
    private int rentableSlots;
    private int slots;
    private int rank;

    public static CachedMineInfo deserialize(GravSerializer serializer) {
        Meta meta = new Meta(serializer);
        return new CachedMineInfo(
                meta.get("gN"),
                meta.get("rP"), //Small size identifier to save space
                meta.get("rS"),
                meta.get("s"),
                meta.get("r"));
    }

    @Override
    public void serialize(GravSerializer serializer) {
        Meta meta = new Meta();
        meta.set("gN", gangName);
        meta.set("rP", rentPrice);
        meta.set("rS", rentableSlots);
        meta.set("s", slots);
        meta.set("r", rank);
        meta.serialize(serializer);
    }
}
