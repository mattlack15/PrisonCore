package com.soraxus.prisons.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class ActionBar {

    private static class ActionBarInfo {
        private long sentTime;
        private int priority;

        ActionBarInfo(long sentTime, int priority) {
            this.sentTime = sentTime;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public long getSentTime() {
            return sentTime;
        }
    }

    private static Map<UUID, ActionBarInfo> lastActionBars = new HashMap<>();

    /**
     * Send an actionbar message to the player
     *
     * @param player  Player
     * @param message Message
     */
    public static void send(@NotNull Player player, @NotNull String message) {
        send(player, message, 0);
    }

    /**
     * Send an actionbar message to the player
     *
     * @param player  Player
     * @param message Message
     */
    public static void send(@NotNull Player player, @NotNull String message, int priority) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (lastActionBars.containsKey(player.getUniqueId()) && lastActionBars.get(player.getUniqueId()).getPriority() > priority && System.currentTimeMillis() - lastActionBars.get(player.getUniqueId()).getSentTime() < 2000)
            return;

        lastActionBars.put(player.getUniqueId(), new ActionBarInfo(System.currentTimeMillis(), priority));

        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);

        //1.10 and up
        if (!nmsVersion.startsWith("v1_9_R") && !nmsVersion.startsWith("v1_8_R")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            return;
        }

        //1.8.x and 1.9.x
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> ppoc = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
            Object packetPlayOutChat;
            Class<?> chat = Class.forName("net.minecraft.server." + nmsVersion + (nmsVersion.equalsIgnoreCase("v1_8_R1") ? ".ChatSerializer" : ".ChatComponentText"));
            Class<?> chatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");

            Method method = null;
            if (nmsVersion.equalsIgnoreCase("v1_8_R1")) method = chat.getDeclaredMethod("a", String.class);

            Object object = nmsVersion.equalsIgnoreCase("v1_8_R1") ? chatBaseComponent.cast(method.invoke(chat, "{'text': '" + message + "'}")) : chat.getConstructor(new Class[]{String.class}).newInstance(message);
            packetPlayOutChat = ppoc.getConstructor(new Class[]{chatBaseComponent, Byte.TYPE}).newInstance(object, (byte) 2);

            Method handle = craftPlayerClass.getDeclaredMethod("getHandle");
            Object iCraftPlayer = handle.invoke(craftPlayer);
            Field playerConnectionField = iCraftPlayer.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(iCraftPlayer);
            Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", packet);
            sendPacket.invoke(playerConnection, packetPlayOutChat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send an actionbar message to all online players
     *
     * @param message Message
     */
    public static void sendAll(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> send(p, message));
    }

    /**
     * Send an actionbar message to all online players
     *
     * @param message Message
     */
    public static void sendAll(String message, int priority) {
        Bukkit.getOnlinePlayers().forEach(p -> send(p, message, priority));
    }
}