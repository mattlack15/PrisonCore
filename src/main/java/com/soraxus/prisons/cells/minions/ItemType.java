package com.soraxus.prisons.cells.minions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Objects;


@Getter
@AllArgsConstructor
public class ItemType implements GravSerializable {
    private Material mat;
    private byte data;

    public ItemType(Material mat) {
        this(mat, (byte) 0);
    }

    public ItemType(GravSerializer serializer) {
        this.mat = serializer.readObject();
        this.data = serializer.readByte();
    }

    public static ItemType fromMaterialData(MaterialData data) {
        return new ItemType(data.getItemType(), data.getData());
    }

    public static ItemType fromItem(ItemStack item) {
        return fromMaterialData(item.getData());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(mat);
        serializer.writeByte(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemType itemType = (ItemType) o;
        return data == itemType.data &&
                mat == itemType.mat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mat, data);
    }

    public MaterialData asMaterialData() {
        return new MaterialData(this.mat, this.data);
    }
}
