package com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp;

import com.soraxus.prisons.bunkers.base.army.BunkerArmy;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MenuArmyCamp extends Menu {

    private final ElementArmyCamp camp;

    public MenuArmyCamp(ElementArmyCamp camp) {
        super("Army Camp", 5);
        this.camp = camp;
        this.setup();
    }

    public void setup() {
        this.setAll(null);
        List<BunkerNPC> npcList = camp.getNPCs();
        BunkerArmy army = new BunkerArmy(camp.getBunker(), npcList);
        npcList.sort(Comparator.comparingInt(a -> a.getType().ordinal()));
        AtomicReference<BunkerNPCType> type = new AtomicReference<>();
        AtomicInteger i = new AtomicInteger();
        this.setupActionableList(10, 35, 36, 44, (index) -> {
            BunkerNPC npc = null;
            while(npc == null || npc.getType() == type.get()) {
                int npcIndex = i.getAndIncrement();
                if (npcIndex >= npcList.size())
                    return null;
                npc = npcList.get(npcIndex);
            }
            type.set(npc.getType());
            int count = army.getCount(type.get());
            ItemBuilder builder = new ItemBuilder(type.get().getDisplayItem());
            builder.addLore("", "&fCount: &a" + count);
            builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            return new MenuElement(builder.build());
        },0);

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i1) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        MenuManager.instance.invalidateInvsForMenu(this);
    }
}
