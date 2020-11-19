package com.soraxus.prisons.cells.shop;

import com.soraxus.prisons.util.list.LockingList;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

public class CustomShop implements GravSerializable {

    @Getter
    private final LockingList<ShopItem> items = new LockingList<>();

    @Getter
    @Setter
    private String name;

    @Getter
    private Meta meta = new Meta();

    public CustomShop(String name) {
        this.name = name;
    }

    public CustomShop(GravSerializer serializer) {
        this.name = serializer.readString();
        this.meta = new Meta(serializer);
        this.items.addAll(serializer.readObject());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(this.name);
        meta.serialize(serializer);
        serializer.writeObject(this.items);
    }
}
