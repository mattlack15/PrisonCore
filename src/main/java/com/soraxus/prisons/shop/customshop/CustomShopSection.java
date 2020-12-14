package com.soraxus.prisons.shop.customshop;

import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.list.LockingList;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;
import org.bukkit.inventory.ItemStack;

public class CustomShopSection implements GravSerializable {

    @Getter
    private final LockingList<ShopItem> items = new LockingList<>();

    @Getter
    @Setter
    private volatile String name;

    @Getter
    private Meta meta = new Meta();

    @Getter
    @Setter
    private volatile ItemStack displayStack;

    public CustomShopSection(String name, ItemStack displayItem) {
        this.name = name;
        this.displayStack = displayItem;
    }

    public CustomShopSection(GravSerializer serializer) {
        this.name = serializer.readString();
        this.displayStack = ItemBuilder.itemFromString(serializer.readString());
        this.meta = new Meta(serializer);
        this.items.addAll(serializer.readObject());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(this.name);
        serializer.writeString(ItemBuilder.itemToString(displayStack));
        meta.serialize(serializer);
        serializer.writeObject(this.items);
    }
}
