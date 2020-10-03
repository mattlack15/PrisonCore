package com.soraxus.prisons.bunkers.gui;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.ModuleBunkers;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.matchmaking.BunkerMatchSelector;
import com.soraxus.prisons.bunkers.tools.ToolUtils;
import com.soraxus.prisons.enchants.manager.CooldownUtils;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangRole;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.time.DateUtils;
import com.soraxus.prisons.util.time.Timer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class MenuBunker extends Menu {
    private Gang gang;

    public MenuBunker(Gang gang) {
        super("Your Bunker", 5);
        if (gang == null)
            throw new IllegalArgumentException("Gang must not be null!");
        this.gang = gang;
    }

    private MenuElement backButton = null;

    public MenuBunker(Gang gang, MenuElement backButton) {
        super(gang.getName() + "'s Bunker", 5);
        this.gang = gang;
        this.backButton = backButton;
    }

    @Override
    public void open(HumanEntity p, Object... data) {
        setup((Player) p);
        super.open(p, data);
    }

    private void setup(Player player) {

        this.setAll(null);
        this.fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .setClickHandler((e, i) -> ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1f)));

        boolean inBunker = ModuleBunkers.isBunkerWorld(player.getLocation().getWorld().getName());
        MenuElement teleport = new MenuElement(new ItemBuilder(inBunker ? Material.ENDER_PEARL : Material.EYE_OF_ENDER)
                .setName("§a&lTeleport")
                .addLore(
                        "§7Click to teleport " + (!inBunker ? "to" : "out of") + " your bunker!"
                ).build()
        ).setClickHandler((e, i) -> {
            Player pl = (Player) e.getWhoClicked();
            getAsyncExecutor().execute(() -> {
                Bunker bunker = gang.getBunker();
                if (inBunker && bunker != null) {
                    e.getWhoClicked().sendMessage(PREFIX + "Teleporting...");
                    Bunker finalBunker1 = bunker;
                    Synchronizer.synchronize(() -> finalBunker1.teleportBack((Player) e.getWhoClicked()));
                    return;
                }
                if (bunker == null) {
                    try {
                        pl.sendMessage(PREFIX + "Loading bunker...");
                        bunker = BunkerManager.instance.createOrLoadBunker(gang).get();
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                    }
                }
                e.getWhoClicked().sendMessage(PREFIX + "Teleporting...");
                Bunker finalBunker = bunker;
                Synchronizer.synchronize(() -> {
                    if (finalBunker == null) {
                        return;
                    }
                    finalBunker.teleport(pl);
                });
            });
        });

        MenuElement tool = new MenuElement(new ItemBuilder(Material.STICK, 1).setName("&6&lGet Tool")
                .addLore("&7Click this to get the bunker tool!").build()).setClickHandler((e, i) -> {
            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.1f);
            e.getWhoClicked().getInventory().addItem(ToolUtils.getDefaultTool());
        });

        MenuElement buildTool = new MenuElement(new ItemBuilder(Material.BLAZE_ROD, 1).setName("&6&lGet Quick-Build Tool")
                .addLore("&7Click this to get the quick-build tool!").build()).setClickHandler((e, i) -> {
            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.1f);
            e.getWhoClicked().getInventory().addItem(ToolUtils.getBuildTool(null));
        });

        MenuElement info = new MenuElement(new ItemBuilder(Material.SIGN, 1)
                .setName("&a&lInfo")
                .addLore("&fRating: &7" + (gang.getBunker() != null ? gang.getBunker().getRating() : "bunker unloaded"))
                .addLore("&fCurrently Loaded Bunkers: &c" + BunkerManager.instance.getBunkerWeakList().size())
                .build()
        ).setClickHandler((event, info1) -> {
        });
        MenuElement reload = new MenuElement(new ItemBuilder(Material.ANVIL, 1)
                .setName("&c&lRegenerate Bunker")
                .addLore("&7This will reload your bunker")
                .addLore("&7You will be removed from the bunker during this process")
                .build()).setClickHandler((e, i) -> getAsyncExecutor().execute(() -> {
            if (!CooldownUtils.isCooldown(e.getWhoClicked(), "reloadBunker")) {
                String wait = DateUtils.convertTimeM(CooldownUtils.getCooldownTime(e.getWhoClicked(), "reloadBunker"));
                this.getElement(e.getSlot()).addTempLore(this, "§cPlease wait " + wait + " before doing that again", 60);
                return;
            }

            Timer timer = new Timer();
            if (!e.getWhoClicked().hasPermission("bunkers.reload.bypasscooldown"))
                CooldownUtils.setCooldown(e.getWhoClicked(), "reloadBunker", 5);
            Bunker bunker = gang.getBunker();
            if (bunker == null) {
                try {
                    BunkerManager.instance.createOrLoadBunker(gang).get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }
            } else {
                List<UUID> previouslyThere = bunker.getWorld().getBukkitWorld().getPlayers().stream()
                        .map(Player::getUniqueId)
                        .collect(Collectors.toList());
                try {
                    bunker.unload().get();
                } catch (InterruptedException | ExecutionException interruptedException) {
                    interruptedException.printStackTrace();
                }
                try {
                    bunker = BunkerManager.instance.createOrLoadBunker(gang).get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }
                Bunker finalBunker = bunker;
                previouslyThere.forEach(p -> {
                    Player pl = Bukkit.getPlayer(p);
                    if (pl != null) {
                        Synchronizer.synchronize(() -> {
                            if (finalBunker == null) {
                                Bukkit.broadcastMessage("finalBunker is NULL Approx. ln 156");
                                return;
                            }
                            finalBunker.teleport(pl);
                        });
                    }
                });
            }
            timer.stop();
            this.getElement(e.getSlot()).addTempLore(this, "&aCompleted in &f" + (timer.getTimeMillis() / 1000f) + "s&a!", 60);
        }));

        MenuElement findMatch = new MenuElement(new ItemBuilder(Material.DIAMOND_SWORD, 1).setName("&cAttack").addLore("&7Click to search for a bunker to attack!").build())
                .setClickHandler((e, i) -> {
                    e.getWhoClicked().closeInventory();
                    new BunkerMatchSelector((Player) e.getWhoClicked());
                });

        MenuElement deleteBunker = new MenuElement(new ItemBuilder(Material.REDSTONE_BLOCK).setName("&4&lDelete Bunker")
                .addLore("&7Deletes this bunker.", "", "&cNOTE: &7This is permanent and your bunker", "&7cannot be restored, you must restart.").build())
                .setClickHandler((e, i) -> {
                    if (!gang.isMember(e.getWhoClicked().getUniqueId())) {
                        getElement(e.getSlot()).addTempLore(this, "&cYou must be a member!", 60);
                        return;
                    }
                    for (GangMember member : gang.getMembers()) {
                        if (member.getMember().equals(e.getWhoClicked().getUniqueId())) {
                            if (member.getGangRole() != GangRole.LEADER) {
                                getElement(e.getSlot()).addTempLore(this, "&cYou must be the leader of the gang!", 60);
                                return;
                            }
                        }
                    }
                    boolean hasBunker;
                    if(hasBunker = (gang.getBunker() != null))
                        e.getWhoClicked().closeInventory();
                    BunkerManager.instance.deleteBunkerAsync(gang.getId()).thenAccept((unused) -> {
                        if (hasBunker)
                            gang.broadcastMessage("The gang bunker has been&c deleted&7 by &e" + e.getWhoClicked().getName());
                    });
                });

        this.setElement(4, backButton);
        this.setElement(10, teleport);
        this.setElement(12, info);
        this.setElement(14, tool);
        this.setElement(16, buildTool);
        this.setElement(28, reload);
        this.setElement(30, findMatch);
        this.setElement(32, deleteBunker);
    }
}
