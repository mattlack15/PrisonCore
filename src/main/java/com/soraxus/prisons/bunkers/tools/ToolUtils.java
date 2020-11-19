package com.soraxus.prisons.bunkers.tools;

import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.items.NBTUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ToolUtils {
    // Default tool (Edit wand)
    public static ItemStack getDefaultTool() {
        return new ItemBuilder(Material.STICK)
                .setName("§aEdit Wand")
                .addLore(
                        "§7Right click an element to do stuff"
                )
                .addNBT("type", "wand_edit")
                .build();
    }

    public static boolean isDefaultTool(ItemStack item) {
        return ItemUtils.isType(item, "wand_edit");
    }


    // Build tool
    public static ItemStack getBuildTool(BunkerElementType type) {
        return new ItemBuilder(type == null ? new ItemStack(Material.BLAZE_ROD) : type.getInfo().getShopInfo().getItem())
                .setName("§aBuild Tool")
                .addLore(
                        "§7Left click to select an element to build with",
                        "§7Right click to place the selected item"
                )
                .addNBT("type", "wand_build")
                .addNBT("element", "TODO")
                .build();
    }

    public static boolean isBuildTool(ItemStack item) {
        return ItemUtils.isType(item, "wand_build");
    }

    public static String getElement(ItemStack item) {
        return NBTUtils.instance.getString(item, "element");
    }
}
