package com.soraxus.prisons.util.display.chat;

import net.md_5.bungee.api.chat.*;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ClickUtil {
    public static ClickEvent url(String url) {
        return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
    }

    /**
     * Open a file, not that this file must be on the player's computer, for example a screenshot
     * Not usually a good choice to use server-side
     *
     * @param file File
     * @return Click event
     */
    public static ClickEvent file(String file) {
        return new ClickEvent(ClickEvent.Action.OPEN_FILE, file);
    }

    /**
     * Change the page of a book, no clue what it does in chat
     *
     * @param page Page
     * @return Click event
     */
    public static ClickEvent page(String page) {
        return new ClickEvent(ClickEvent.Action.CHANGE_PAGE, page);
    }

    /**
     * Run a command
     *
     * @param command Command
     * @return Click event
     */
    public static ClickEvent command(String command) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
    }

    /**
     * Suggest a command
     *
     * @param command Command
     * @return Click event
     */
    public static ClickEvent commandSuggest(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }
}
