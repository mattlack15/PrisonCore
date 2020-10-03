package com.soraxus.prisons.util.display.chat;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

public class ChatBuilder {
    private TextComponent comp;

    public ChatBuilder() {
        comp = new TextComponent();
    }

    public ChatBuilder(String text) {
        comp = fromString(text);
    }

    public ChatBuilder addText(String str) {
        comp.addExtra(fromString(str));
        return this;
    }
    public ChatBuilder addText(String str, ClickEvent e) {
        TextComponent c = fromString(str);
        c.setClickEvent(e);
        comp.addExtra(c);
        return this;
    }
    public ChatBuilder addText(String str, HoverEvent e) {
        TextComponent c = fromString(str);
        c.setHoverEvent(e);
        comp.addExtra(c);
        return this;
    }
    public ChatBuilder addText(String str, HoverEvent e, ClickEvent e2) {
        TextComponent c = fromString(str);
        c.setHoverEvent(e);
        c.setClickEvent(e2);
        comp.addExtra(c);
        return this;
    }

    public TextComponent build() {
        return comp;
    }

    private static TextComponent fromString(String text) {
        return new TextComponent(
                TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes('&', text)
                )
        );
    }
}
