package com.soraxus.prisons.util.display.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class HoverUtil {
    /**
     * Generate a text hover
     *
     * @param str Text
     * @return Hover event with text
     */
    public static HoverEvent text(String str) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes('&', str))
        );
    }

    /**
     * Generate an entity hover
     *
     * @param entity Entity
     * @return Hover event with entity
     */
    public static HoverEvent entity(Entity entity) {
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new ComponentBuilder("{type:\"" + entity.getType().getName() + "\", id:\"" + entity.getUniqueId() + "\"}").create());
    }

    /**
     * Generate cross-version item hover
     * Note: This only shows name and lore,
     * enchantments and attributes may not
     * be shown
     *
     * @param item Item
     * @return Hover event with item
     */
    public static HoverEvent itemNew(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack it = CraftItemStack.asNMSCopy(item);
        String text = it.getName();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            text += "\n" + String.join("\n", item.getItemMeta().getLore());
        }
        return text(text);
    }

    /**
     * Generate a true item hover (may break for older versions)
     *
     * @param item Item
     * @return Hover event with item
     */
    public static HoverEvent item(ItemStack item) {
        NBTTagCompound compound = new NBTTagCompound();
        net.minecraft.server.v1_12_R1.ItemStack it = CraftItemStack.asNMSCopy(item);
        it.save(compound);
        BaseComponent[] hover = {new TextComponent(compound.toString())};
        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, hover);
    }
}
