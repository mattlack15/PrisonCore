package com.soraxus.prisons.gangs.gui;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangRole;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class MenuGang extends Menu {
    private final GangRole permissionLevel;
    private final MenuElement backButton;
    private final Gang gang;

    public MenuGang(Gang gang, GangRole permissionLevel, MenuElement backButton) {
        super(gang.getName(), 3);
        this.permissionLevel = permissionLevel;
        this.backButton = backButton;
        this.gang = gang;
        this.setup();
    }

    public void setup() {
        MenuElement disband = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("&c&lDisband Gang").addLore("&7Click to disband this gang", "", "&cNOTE: &7this cannot be undone", "&8Must be leader").build())
                .setClickHandler((e, i) -> {
                    if (this.permissionLevel != GangRole.LEADER) {
                        getElement(e.getSlot()).addTempLore(this, "&cYou must be the leader!", 60);
                        return;
                    }
                    gang.broadcastMessage(PREFIX + "Your gang was disbanded by &e" + e.getWhoClicked().getName() + "!");
                    gang.disband();
                });
    }
}
