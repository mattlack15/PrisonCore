package com.soraxus.prisons.shop.customshop;

import com.soraxus.prisons.util.list.LockingList;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

public class CustomShop implements GravSerializable {
    @Getter
    private final LockingList<CustomShopSection> sections = new LockingList<>();

    @Getter
    @Setter
    private volatile String name;

    public CustomShop(String name) {
        this.name = name;
    }

    public CustomShop(GravSerializer serializer) {
        sections.addAll(serializer.readObject());
        name = serializer.readString();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(sections);
        serializer.writeString(name);
    }
}
