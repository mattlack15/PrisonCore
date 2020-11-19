package com.soraxus.prisons.bunkers.base.elements.defense.active.barracks;

import com.soraxus.prisons.bunkers.matchmaking.MatchInfo;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import com.soraxus.prisons.util.time.DateUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuBarracks extends Menu {
    private final ElementBarracks barracks;

    public MenuBarracks(ElementBarracks barracks) {
        super(barracks.getName(), 5);
        this.barracks = barracks;
        this.setup();
    }

    public void setup() {
        this.setAll(null);
        List<BunkerNPCType> available = barracks.getAvailableTypes();
        this.setupActionableList(10, 26, 27, 35, (index -> {
            if (BunkerNPCType.values().length <= index)
                return null;
            BunkerNPCType type = BunkerNPCType.values()[index];
            if (available.contains(type)) {
                int level = barracks.getBunker().getNpcSkillManager().getLevel(type);
                return new MenuElement(getItem(type, level)).setClickHandler((e, i) -> {
                    if (barracks.getBunker().hasResources(type.getCost(level))) {
                        barracks.getBunker().removeResources(type.getCost(1));
                        barracks.startGeneration(type, level);
                        setup();
                    }
                }).setDoUpdates(true).setUpdateEvery(20).setUpdateHandler((e) -> e.setItem(getItem(type, level)));
            } else {
                ItemBuilder builder = new ItemBuilder(Material.BEDROCK, 1).setName("&c???")
                        .addLore("&8" + type.getDescription());
                return new MenuElement(builder.build());
            }
        }), 0);

        MatchInfo lastMatchInfo = null;
        MenuElement lastMatch = null;
        if (barracks.getBunker().getMatchStatistics().getTotalMatches() != 0)
            lastMatchInfo = barracks.getBunker().getMatchStatistics().getLastMatch();
        if (lastMatchInfo != null) {
            boolean attacker = lastMatchInfo.getAttackingBunker().equals(barracks.getBunker().getId());
            boolean won = lastMatchInfo.getMatchStats().getAttackerSuccess() ^ (!attacker);
            lastMatch = new MenuElement(new ItemBuilder(Material.ENCHANTED_BOOK, 1).setName("&8Last Match")
                    .addLore("",
                            "&7Role: &f" + (lastMatchInfo.getAttackingBunker() == barracks.getBunker().getId() ? "Attacker" : "Defender"),
                            "&7Duration: &f" + DateUtils.readableDate(lastMatchInfo.getMatchStats().getMatchDurationSeconds(), true),
                            "&7Result: " + (won ? "&aVictory" : "&cDefeat")).build());
        }

        this.setElement(4, barracks.getInfoElement());
        this.setElement(8, lastMatch);

        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        MenuManager.instance.invalidateInvsForMenu(this);
    }

    private ItemStack getItem(BunkerNPCType type, int level) {
        int ticks = 0;
        int amount = 0;
        for (ProcessNPCTraining training : barracks.getTrainingListByType(type)) {
            ticks += training.getTicksLeft();
            amount++;
        }
        return new ItemBuilder(type.getDisplayItem()).setName("&f&l" + type.getDisplayName())
                .addLore("&7" + type.getDescription())
                .addLore("")
                .addLore("&fLevel: &7" + level + "&f/&7" + type.getInfo().getMaxLevel())
                .addLore("")
                .addLore("&cCurrently training: &f" + amount + " &7(" + DateUtils.readableDate(ticks / 20, true) + ")")
                .build();
    }
}
