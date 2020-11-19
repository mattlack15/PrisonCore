package com.soraxus.prisons.util.string;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextUtil {
    public static String[] splitIntoLines(String str, String regexWordSeperator, String lineBeginning, int maxLineSize) {
        if (str.length() <= maxLineSize) {
            return new String[]{lineBeginning + str};
        }
        ArrayList<String> out = new ArrayList<>();
        String[] words = str.split(" ");
        StringBuilder holder = new StringBuilder(lineBeginning + "");
        for (String word : words) {
            if (holder.length() + word.length() >= maxLineSize) {
                out.add(holder.toString());
                holder = new StringBuilder(lineBeginning + word + " ");
            } else {
                holder.append(word).append(" ");
            }
        }
        out.add(holder.toString());
        return out.toArray(new String[0]);
    }

    public static String[] splitIntoLines(String str, int maxLineSize) {
        return TextUtil.splitIntoLines(str, " ", "", maxLineSize);
    }

    public static String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static List<String> color(List<String> in) {
        return in.stream()
                .map(TextUtil::color)
                .collect(Collectors.toList());
    }


    public static String generateBar(char onColor, char offColor, int length, int curr, int max) {
        return generateBar(onColor, offColor, length, BigInteger.valueOf(curr), BigInteger.valueOf(max));
    }

    public static String generateBar(char onColor, char offColor, int length, BigInteger curr, BigInteger max) {
        return generateBar(onColor, offColor, ':', length, curr, max);
    }

    public static String generateBar(char onColor, char offColor, char ch, int length, long curr, long max) {
        return generateBar(onColor, offColor, ch, length, BigInteger.valueOf(curr), BigInteger.valueOf(max));
    }

    @NotNull
    public static String generateBar(char onColor, char offColor, char ch, int length, @NotNull BigInteger curr, BigInteger max) {
        int achieved = curr.multiply(BigInteger.valueOf(100L)).divide(max).multiply(BigInteger.valueOf(length)).divide(BigInteger.valueOf(100L)).intValue();
        if (curr.compareTo(max) >= 0) {
            achieved = length;
        }
        if (curr.compareTo(BigInteger.ZERO) <= 0) {
            achieved = 0;
        }
        StringBuilder str = new StringBuilder("§" + onColor);
        for (int i = 0; i < achieved; i++) {
            str.append(ch);
        }
        str.append("§").append(offColor);
        for (int i = achieved; i < length; i++) {
            str.append(ch);
        }
        return str.toString();
    }


    public static String toText(Location loc) {
        return "(" + loc.getWorld() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")";
    }

    public static String toText(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }


    public static boolean isCommand(String str, String command) {
        return (str.equalsIgnoreCase(command) || str.toLowerCase().startsWith(command.toLowerCase() + " "));
    }


    public static BaseComponent createVoteLink(String url) {
        TextComponent comp = new TextComponent("§7- §e" + url);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("me!")));
        return comp;
    }

    public static String insertDashUUID(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, "-");
        sb.insert(13, "-");
        sb.insert(18, "-");
        sb.insert(23, "-");
        return sb.toString();
    }

    public static String colorBoolean(boolean bl) {
        return bl ? "§a" : "§c";
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String join(String[] in, int start) {
        return join(in, start, in.length);
    }

    public static String join(String[] in, int start, int end) {
        String[] newIn = new String[end - start];
        System.arraycopy(in, start, newIn, 0, end - start);
        return String.join(" ", newIn);
    }
}
