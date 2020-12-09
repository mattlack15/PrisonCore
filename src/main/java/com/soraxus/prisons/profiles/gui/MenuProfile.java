package com.soraxus.prisons.profiles.gui;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;
import com.soraxus.prisons.profiles.PrisonProfile;
import com.soraxus.prisons.profiles.ProfileComment;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import com.soraxus.prisons.util.string.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MenuProfile extends Menu {

    private static final Map<UUID, Consumer<String>> listeners = new ConcurrentHashMap<>();

    private final PrisonProfile profile;
    private final String playerName;
    private final UUID opener;

    public MenuProfile(UUID opener, PrisonProfile profile) {
        this(opener, Bukkit.getPlayer(profile.getPlayerId()) != null ? Bukkit.getPlayer(profile.getPlayerId()).getName()
                : profile.getPlayerId().toString(), profile);
    }

    public MenuProfile(UUID opener, String profileDisplayName, PrisonProfile profile) {
        super(profileDisplayName, 5);

        this.playerName = profileDisplayName;

        this.profile = profile;
        this.opener = opener;

        EventSubscriptions.instance.subscribe(this);

        this.setup();
    }

    public void setup() {

        setAll(null);

        ItemBuilder infoBuilder = new ItemBuilder(Material.BAKED_POTATO);

        if (Bukkit.getPlayer(profile.getPlayerId()) != null) {
            infoBuilder.setupAsSkull(Bukkit.getPlayer(profile.getPlayerId()).getName());
        } else if (playerName.length() <= 16) {
            infoBuilder.setupAsSkull(playerName);
        }

        infoBuilder.setName("&a&l" + playerName);
        infoBuilder.addLore("&8Balance &f$" + NumberUtils.toReadableNumber(Economy.money.getBalance(profile.getPlayerId())));

        GangMember member = GangMemberManager.instance.getOrLoadMember(profile.getPlayerId(), false);
        if (member != null && member.getGang() != null) {
            Gang gang = GangManager.instance.getLoadedGang(member.getGang());
            if (gang != null)
                infoBuilder.addLore("&8Gang &f" + gang.getName());
        }

        this.setElement(0, new MenuElement(infoBuilder.build()));

        if (profile.getPlayerId().equals(opener)) {
            ItemBuilder builder = new ItemBuilder(Material.MAP).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .setName("&6Comment Setting").addLore("");

            for (PrisonProfile.CommentSetting setting : PrisonProfile.CommentSetting.values()) {
                if (profile.getCommentSetting() == setting) {
                    builder.addLore("&f▶ &a" + setting.getDesc());
                } else {
                    builder.addLore("&8▶ &7" + setting.getDesc());
                }
            }

            builder.addLore("", "&8Click to change");

            this.setElement(11, new MenuElement(builder.build()).setClickHandler((e, i) -> {
                if (e.getClick() == ClickType.DOUBLE_CLICK)
                    return;
                int index = profile.getCommentSetting().ordinal();
                if (++index >= PrisonProfile.CommentSetting.values().length)
                    index = 0;
                profile.setCommentSetting(PrisonProfile.CommentSetting.values()[index]);
                setup();
            }));
        }

        ItemBuilder builder = new ItemBuilder(Material.NAME_TAG);

        if (profile.getCommentSetting() == PrisonProfile.CommentSetting.ALLOW) {
            builder.setName("&aPost Comment").addLore("&7Click to post a comment!").build();
        } else {
            builder.setName("&c&lComments disabled").addLore("&7Sorry D:");
        }

        this.setElement(13, new MenuElement(builder.build())
                .setClickHandler((e, i) -> {
                    if (profile.getCommentSetting() != PrisonProfile.CommentSetting.ALLOW)
                        return;
                    e.getWhoClicked().closeInventory();
                    listeners.put(e.getWhoClicked().getUniqueId(), (s) -> {
                        profile.addComment(new ProfileComment(System.currentTimeMillis(), s, e.getWhoClicked().getUniqueId(), e.getWhoClicked().getName(), false));
                        this.setup();
                        this.open(e.getWhoClicked());
                        Player player = Bukkit.getPlayer(profile.getPlayerId());
                        if (player != null) {
                            Synchronizer.synchronize(() -> {
                                player.sendMessage("");
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&c&lProfile > &a" + e.getWhoClicked().getName() + "&f has posted a comment on your profile:"));
                                player.sendMessage(s);
                                player.sendMessage("");
                            });
                        }
                    });
                    e.getWhoClicked().sendMessage(ChatColor.YELLOW + "Enter your comment in chat:");
                }));

        List<ProfileComment> commentList = profile.getComments();
        if (!profile.getPlayerId().equals(opener))
            commentList.removeIf(ProfileComment::isPrivateComment);

        if (profile.getCommentSetting() != PrisonProfile.CommentSetting.DISALLOW) {
            this.setupActionableList(19, 19 + 15, 19 + 15 + 3, 19 + 15 + 3 + 6,
                    (index) -> {
                        if (commentList.size() <= index)
                            return null;
                        ProfileComment comment = commentList.get(index);
                        if (comment.getCommenter().equals(opener)) {
                            return new MenuElement(new ItemBuilder(Material.EMPTY_MAP).setName("&6" + comment.getCommenterDisplayName() + (comment.isPrivateComment() ? " &c&lPRIVATE" : ""))
                                    .addLore(TextUtil.splitIntoLines(comment.getComment(), " ", "&f", 40))
                                    .addLore("", "&8Posted on &f" + new SimpleDateFormat("dd/MM/yyyy").format(new Date(comment.getCreationTime())))
                                    .addLore("", "&c&lShift-Click to remove your comment").build())
                                    .setClickHandler((e, i) -> {
                                        if (e.getClick().isShiftClick()) {
                                            profile.removeComment(comment);
                                            this.setup();
                                        } else {
                                            getElement(e.getSlot()).addTempLore(this, "&c&nShift&7-Click to remove you silly goose.", 60);
                                        }
                                        ;
                                    });
                        } else {
                            return new MenuElement(new ItemBuilder(Material.PAPER).setName("&6" + comment.getCommenterDisplayName() + (comment.isPrivateComment() ? " &c&lPRIVATE" : ""))
                                    .addLore(TextUtil.splitIntoLines(comment.getComment(), " ", "&f", 40))
                                    .addLore("", "&8Posted on &f" + new SimpleDateFormat("dd/MM/yyyy").format(new Date(comment.getCreationTime()))).build());
                        }
                    }, 0);
        }

        MenuManager.instance.invalidateInvsForMenu(this);
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> c = listeners.remove(event.getPlayer().getUniqueId());
        if (c != null) {
            event.setCancelled(true);
            try {
                c.accept(event.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventSubscription
    private void onQuit(PlayerQuitEvent event) {
        listeners.remove(event.getPlayer().getUniqueId());
    }
}
