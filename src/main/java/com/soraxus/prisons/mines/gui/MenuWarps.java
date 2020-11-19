package com.soraxus.prisons.mines.gui;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.cells.Cell;
import com.soraxus.prisons.cells.CellManager;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuWarps extends Menu {

    private UUID playerId;

    public MenuWarps(UUID playerId) {
        super("Warps", 3);
        this.playerId = playerId;
        this.setup();
    }

    public void setup() {
        MenuElement mines = new MenuElement(new ItemBuilder(Material.GOLD_PICKAXE).setName("&a&lMines").build()).setClickHandler((e, i) -> new MenuWarpsMines(e.getWhoClicked().getUniqueId(), getBackButton(this)).open(e.getWhoClicked()));
        MenuElement bunker = new MenuElement(new ItemBuilder(Material.EYE_OF_ENDER).setName("&a&lBunker").build()).setClickHandler((e, i) -> {
            GangMember member = GangMemberManager.instance.getOrLoadMember(playerId);
            if(member.getGang() == null)
                return;
            Gang gang = GangManager.instance.getLoadedGang(member.getGang());
            if(gang == null)
                return;
            Bunker b = gang.getBunker();
            b.teleport((Player) e.getWhoClicked());
        });

        MenuElement cell = new MenuElement(new ItemBuilder(Material.IRON_BARDING).setName("&a&lCell").build()).setClickHandler((e, i) -> {
            Cell c = CellManager.instance.getLoadedCell(playerId);
            if(c == null)
                return;
            ((Player)e.getWhoClicked()).performCommand("cell tp");
        });

        this.setElement(11, mines);
        this.setElement(13, cell);
        this.setElement(15, bunker);
    }
}
