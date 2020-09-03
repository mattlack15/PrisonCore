package com.soraxus.prisons.util.display;

import com.soraxus.prisons.util.string.TextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ComponentUtil {
	@NotNull
	public static TextComponent getClickHoverComponent(String text, @NotNull String hover, ClickEvent.Action ac, String click) {
		text = ChatColor.translateAlternateColorCodes('&', text);
		hover = ChatColor.translateAlternateColorCodes('&', hover);

		TextComponent component = new TextComponent(TextComponent.fromLegacyText(text));
		String[] description = hover.split("<nl>");
		TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
		TextComponent hover1 = new TextComponent(TextUtil.color(description[0]));
		for(int i = 1; i < description.length; i++) {
			hover1.addExtra(newLine);
			hover1.addExtra(new TextComponent(TextComponent.fromLegacyText(TextUtil.color(description[i]))));
		}
		ArrayList<TextComponent> components = new ArrayList<>();
		components.add(hover1);
		component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, (BaseComponent[])components.toArray(new BaseComponent[components.size()])));
		component.setClickEvent(new ClickEvent(ac, click));
		return component;
	}

	@NotNull
	public static TextComponent getHoverComponent(String text, @NotNull String hover) {
		text = ChatColor.translateAlternateColorCodes('&', text);
		hover = ChatColor.translateAlternateColorCodes('&', hover);

		TextComponent component = new TextComponent(TextComponent.fromLegacyText(text));
		String[] description = hover.split("<nl>");
		TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
		TextComponent hover1 = new TextComponent(TextUtil.color(description[0]));
		for(int i = 1; i < description.length; i++) {
			hover1.addExtra(newLine);
			hover1.addExtra(new TextComponent(TextComponent.fromLegacyText(TextUtil.color(description[i]))));
		}
		ArrayList<TextComponent> components = new ArrayList<>();
		components.add(hover1);
		component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, components.toArray(new BaseComponent[0])));
		return component;
	}

	/**
	 * Create a TextComponent from a String
	 * @param text String
	 * @return Text Component
	 */
	public static TextComponent toComponent(String text) {
		return new TextComponent(TextComponent.fromLegacyText(text));
	}
}
