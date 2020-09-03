package com.soraxus.prisons.pickaxe.crystals;

import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.items.NBTUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Crystal implements GravSerializable {
    @Setter
    private int index;
    private CrystalType type;
    private int tier;

    public Crystal(GravSerializer serializer) {
        this.index = serializer.readInt();
        this.type = (CrystalType) serializer.readObject();
        this.tier = serializer.readInt();
    }

    public String getDisplayLine() {
        return type.getDisplayName() + " " + tier;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemBuilder(Material.NETHER_STAR)
                .setName(type.getDisplayName())
                .addLore(
                        "" // TODO: Create lore or make config
                )
                .build();
        item = ItemUtils.setType(item, "crystal");
        GravSerializer s = new GravSerializer();
        serialize(s);
        item = NBTUtils.instance.setByteArray(item, "crystal_data", s.toByteArray());
        return item;
    }

    public static boolean isCrystalItem(ItemStack item) {
        return ItemUtils.isType(item, "crystal");
    }
    public static Crystal fromItem(ItemStack item) {
        return new Crystal(new GravSerializer(NBTUtils.instance.getByteArray(item, "crystal_data")));
    }

    public double getPercent() {
        return -1.0; // TODO: Function
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeInt(this.index);
        serializer.writeObject(this.type);
        serializer.writeInt(this.tier);
    }
}
