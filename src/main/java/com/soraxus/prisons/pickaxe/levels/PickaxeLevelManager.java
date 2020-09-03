package com.soraxus.prisons.pickaxe.levels;

import com.soraxus.prisons.util.*;
import com.soraxus.prisons.util.items.NBTUtils;
import com.soraxus.prisons.util.string.TextUtil;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PickaxeLevelManager {

    private static final String TAG_NAME = "spc.pickaxe.level";
    private static final String LORE_IDENTIFIER = ChatColor.translateAlternateColorCodes('&', "&7&e&7&e&7&e&l&e");
    public static PickaxeLevelManager instance;

    public PickaxeLevelManager() {
        instance = this;
        EventSubscriptions.instance.subscribe(this);
    }

    /**
     * Adds XP to the player's currently held item
     */
    public static void addXp(Player player, int amount) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        if (stack != null && stack.getType().toString().contains("PICKAXE")) {
            PickaxeLevelInfo current = getInfo(stack);
            current.increment(amount);
            stack.setItemMeta(apply(stack, current).getItemMeta());
        }
    }

    public static PickaxeLevelInfo getInfo(@NotNull ItemStack stack) {
        GravSerializer serializer = new GravSerializer(NBTUtils.instance.getByteArray(stack, TAG_NAME));
        PickaxeLevelInfo info = new PickaxeLevelInfo();
        if (serializer.hasNext())
            info.read(serializer);
        return info;
    }

    public static ItemStack apply(@NotNull ItemStack stack, @NotNull PickaxeLevelInfo info) {
        GravSerializer serializer = new GravSerializer();
        info.write(serializer);
        ItemStack out = NBTUtils.instance.setByteArray(stack, TAG_NAME, serializer.toByteArray());
        ItemBuilder builder = new ItemBuilder(out);

        int pos = -1;
        List<String> lore = builder.getLore();
        for (String str : lore) {
            String id = LORE_IDENTIFIER.split("\n")[0];
            if (str.startsWith(id)) {
                pos = lore.indexOf(str);
                break;
            }
        }

        String loreMessage = " &f[&a" + info.getLevel() + "&f]   " + TextUtil.generateBar('9', 'f', 20, info.getXp(), info.getRequiredXp()) + "  &f" + info.getXp() + "&7/&f" + info.getRequiredXp();
        String msg = "&f&l&nLevel\n&f";
        if (pos == -1) {
            builder.addLore(LORE_IDENTIFIER);
            String[] id_els = msg.split("\n");
            for (int i = 0; i < id_els.length; i++) {
                builder.addLore(id_els[i] + (i == id_els.length - 1 ? loreMessage : ""));
            }
        } else {
            builder.setLore(pos + msg.split("\n").length, loreMessage);
        }
        return builder.build();
    }

    public int getRequiredXp(int level) {
        return 1000 + (level + 5) * (level);
    }

    //Events
    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        addXp(event.getPlayer(), 1);
    }
}
