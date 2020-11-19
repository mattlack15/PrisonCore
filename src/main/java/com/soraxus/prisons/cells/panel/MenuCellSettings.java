package com.soraxus.prisons.cells.panel;

import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.CellSettings;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MenuCellSettings extends Menu {

    private final Cell parent;
    private final MenuElement backElement;

    public MenuCellSettings(Cell parent, MenuElement backElement) {
        super("Cell Settings", 3);
        this.parent = parent;
        this.backElement = backElement;
        this.setup();
    }

    public void setup() {
        CellSettings settings = parent.getSettings();

        ItemBuilder builder = new ItemBuilder(Material.ENDER_PORTAL_FRAME, 1).setName("&6&lAllow Visitors");
        CellSettings.OpenSetting currentOpenSetting = settings.getOpenSetting();
        if (currentOpenSetting == CellSettings.OpenSetting.OPEN) {
            builder.addLore("&aOpen");
        } else if (currentOpenSetting == CellSettings.OpenSetting.TRUSTED) {
            builder.addLore("&eTrusted");
        } else {
            builder.addLore("&cJust me");
        }
        builder.addLore("", "&7Click to change");

        MenuElement openSetting = new MenuElement(builder.build()).setClickHandler((e, i) -> {
            int o = settings.getOpenSetting().ordinal() + 1;
            if (o >= CellSettings.OpenSetting.values().length)
                o = 0;
            settings.setOpenSetting(CellSettings.OpenSetting.values()[o]);
            this.setup();
        });

        builder = new ItemBuilder(Material.BARRIER, 1).setName("&6&lProtection");
        CellSettings.ProtectionSetting currentProtectionSetting = settings.getProtectionSetting();
        if (currentProtectionSetting == CellSettings.ProtectionSetting.EVERYONE) {
            builder.addLore("&aEveryone can build");
        } else if (currentProtectionSetting == CellSettings.ProtectionSetting.TRUSTED) {
            builder.addLore("&eTrusted members can build");
        } else {
            builder.addLore("&cOnly I can build");
        }
        builder.addLore("", "&7Click to change");

        MenuElement protectionSetting = new MenuElement(builder.build()).setClickHandler((e, i) -> {
            int o = settings.getProtectionSetting().ordinal() + 1;
            if (o >= CellSettings.ProtectionSetting.values().length)
                o = 0;
            settings.setProtectionSetting(CellSettings.ProtectionSetting.values()[o]);
            this.setup();
        });

        MenuElement worldTimeSetting = new MenuElement(new ItemBuilder(Material.WATCH, 1).setName("&6&lWorld Time")
                .addLore(settings.getWorldTime() > 12000 ? "&9Night" : "&eDay").addLore("", "&7Click to change").build()).setClickHandler((e, i) -> {
            boolean day = settings.getWorldTime() <= 12000;
            if (day) {
                parent.setSettingWorldTime(16000);
            } else {
                parent.setSettingWorldTime(0);
            }
            this.setup();
        });

        this.setElement(4, this.backElement);
        this.setElement(11, openSetting);
        this.setElement(13, protectionSetting);
        this.setElement(15, worldTimeSetting);

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
