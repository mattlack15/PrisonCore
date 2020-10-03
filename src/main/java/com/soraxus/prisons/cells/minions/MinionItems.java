package com.soraxus.prisons.cells.minions;

import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.items.NBTUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MinionItems {
    @Getter
    @AllArgsConstructor
    public static class MinionItemData implements GravSerializable {
        private String name;
        private ItemType type;
        private double speed;

        public MinionItemData(GravSerializer serializer) {
            this.name = serializer.readString();
            this.type = new ItemType(serializer);
            this.speed = serializer.readDouble();
        }

        @Override
        public void serialize(GravSerializer serializer) {
            serializer.writeString(name);
            type.serialize(serializer);
            serializer.writeDouble(speed);
        }
    }

    public static ItemStack getMinionItem(String name, ItemType type, double speed) {
        ItemStack stack = new ItemBuilder(Material.SKULL_ITEM).setupAsSkull("Stone")
                .setName("&eMinion").build(); //TODO change
        GravSerializer serializer = new GravSerializer();
        new MinionItemData(name, type, speed).serialize(serializer);
        stack = NBTUtils.instance.setByteArray(stack, "minion_data", serializer.toByteArray());
        return stack;
    }

    public static MinionItemData getData(ItemStack stack) {
        GravSerializer serializer = fromStack(stack);
        if (serializer == null) {
            return null;
        }
        return new MinionItemData(serializer);
    }

    public static boolean isValid(ItemStack stack) {
        return stack != null && NBTUtils.instance.hasTag(stack, "minion_data");
    }

    private static GravSerializer fromStack(ItemStack stack) {
        if (!isValid(stack)) {
            return null;
        }
        return new GravSerializer(NBTUtils.instance.getByteArray(stack, "minion_data"));
    }
}
