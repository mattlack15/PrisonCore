package com.soraxus.prisons.bunkers.tools;

import com.soraxus.prisons.util.items.ItemFactory;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.items.NBTUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ToolUtils {
    // Default tool (Edit wand)
    public static ItemStack getDefaultTool() {
        return new ItemFactory(Material.STICK)
                .setName("§aEdit Wand")
                .setLore(
                        "§7Right click an element to do shit to it"
                )
                .addNBT("type", "wand_edit")
                .create();
    }

    public static boolean isDefaultTool(ItemStack item) {
        return ItemUtils.isType(item, "wand_edit");
    }


    // Build tool
    public static ItemStack getBuildTool(String element) { // TODO: Make this item change to selected item
        return new ItemFactory(Material.NETHER_STAR)
                .setName("§aBuild Tool")
                .setLore(
                        "§7Left click to select an element to build with",
                        "§7Right click to place the selected item"
                )
                .addNBT("type", "wand_build")
                .addNBT("element", "TODO")
                .create();
    }

    public static boolean isBuildTool(ItemStack item) {
        return ItemUtils.isType(item, "wand_build");
    }

    public static String getElement(ItemStack item) {
        return NBTUtils.instance.getString(item, "element");
    }
}
