package com.soraxus.prisons.pickaxe.crystals;

import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.items.NBTUtils;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrystalManager {
    private static final String TAG_NAME = "spc.pickaxe.crystals";

    public static CrystalInfo getInfo(@NotNull ItemStack stack) {
        if (NBTUtils.instance.hasTag(stack, TAG_NAME)) {
            GravSerializer serializer = new GravSerializer(NBTUtils.instance.getByteArray(stack, TAG_NAME));
            CrystalInfo info = new CrystalInfo();
            info.read(serializer);
            return info;
        } else {
            return new CrystalInfo();
        }
    }

    public static ItemStack apply(@NotNull ItemStack stack, @NotNull CrystalInfo info) {
        GravSerializer serializer = new GravSerializer();
        info.write(serializer);
        return NBTUtils.instance.setByteArray(stack, TAG_NAME, serializer.toByteArray());
    }

    public static boolean isCrystal(ItemStack cr) {
        return ItemUtils.isType(cr, "crystal");
    }
}