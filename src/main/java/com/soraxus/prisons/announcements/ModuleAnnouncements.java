package com.soraxus.prisons.announcements;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModuleAnnouncements extends CoreModule {

    private final List<Consumer<ChatBuilder>> announcements = new ArrayList<>();
    private int currentAnnouncement = 0;

    @Override
    protected void onEnable() {
        addAnnouncement((b) -> b.addText("To access the global shop, do &e")
                .addText("/globalshop", HoverUtil.text("&eClick me!"), ClickUtil.command("/globalshop"))
                .sendAll(Bukkit.getOnlinePlayers()));

        Bukkit.getScheduler().runTaskTimer(SpigotPrisonCore.instance, this::announceNext, 40, 20 * 90);
    }

    @Override
    protected void onDisable() {
    }

    public synchronized void announceNext() {
        if(announcements.size() == 0)
            return;
        if(currentAnnouncement >= announcements.size()) {
            currentAnnouncement = 0;
            Collections.shuffle(announcements);
        }

        Consumer<ChatBuilder> announcement = announcements.get(currentAnnouncement);
        announcement.accept(new ChatBuilder("&d&lSoraxus &8&l▶ &7"));

        currentAnnouncement++;
    }

    public void addAnnouncement(Consumer<ChatBuilder> announcement) {
        announcements.add(announcement);
    }

    @Override
    public String getName() {
        return "Announcements";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }
}
