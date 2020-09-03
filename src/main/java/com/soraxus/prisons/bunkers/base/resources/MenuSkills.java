package com.soraxus.prisons.bunkers.base.resources;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Random;

public class MenuSkills extends Menu {
    private MenuElement backButton;
    private Bunker bunker;

    public MenuSkills(Bunker bunker, MenuElement backButton) {
        super("Skillz", 3);
        this.bunker = bunker;
        this.backButton = backButton;
        this.setup();
    }

    public void setup() {
        this.setAll(null);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        for (int i = 2, val = 0; i < 7; i += 2)
            this.setElement(i + 9, getElement(Skill.values()[val++]));

        this.setElement(4, backButton);
        MenuManager.instance.invalidateInvsForMenu(this);
    }

    private Random random = new Random(System.currentTimeMillis());

    private MenuElement getElement(Skill skill) {
        Material mat = null;
        if (skill.equals(Skill.WOODWORKING)) {
            mat = Material.LOG;
        } else if (skill.equals(Skill.STONECUTTING)) {
            mat = Material.COBBLESTONE;
        } else {
            mat = Material.SLIME_BALL;
        }
        int level = bunker.getSkillManager().getSkillLevel(skill);
        boolean canUpgrade = bunker.hasResources(new Storage(skill.getResourceType(), skill.getResearchCost(level), 0));
        ItemBuilder builder = new ItemBuilder(mat, 1)
                .setName("&f&l" + skill.getName())
                .addLore("&7Level: &f" + level)
                .addLore("&7Required: " + skill.getResourceType().getColor() + skill.getResourceType().getDisplayName() + " &f" + skill.getResearchCost(level))
                .addLore("")
                .addLore(canUpgrade ? "&aClick to level up!" : "&cYou are too poor to level up :/")
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (!canUpgrade && random.nextBoolean()) {
            builder.addLore("").addLore("&7Is this some peasant joke I'm").addLore("&7too rich to understand?");
        }

        return new MenuElement(builder.build()).setClickHandler((e, i) -> {
            if (bunker.hasResources(new Storage(skill.getResourceType(), skill.getResearchCost(level), 0))) {
                bunker.removeResources(skill.getResourceType(), skill.getResearchCost(level));
                bunker.getSkillManager().setSkillLevel(skill, level + 1);
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.1f);
                this.setup();
            } else {
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 1.1f);
            }
        });
    }
}
