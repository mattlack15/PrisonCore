package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.errors.ModuleErrors;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.list.LockingList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CmdGangChat extends GangCommand {

    private List<UUID> inGangChat = new LockingList<>();

    public CmdGangChat() {
        this.addAlias("chat");
        this.setRequiresGang(true);
        EventSubscriptions.instance.subscribe(this);
    }

    @Override
    protected void perform() {
        boolean val = inGangChat.contains(getPlayer().getUniqueId());
        if (val) {
            inGangChat.remove(getPlayer().getUniqueId());
            new ChatBuilder(CmdGang.PREFIX + "You have&c exited &e&lGang Chat" + (ChatColor.getLastColors(CmdGang.PREFIX)) + ".").send(getSpigotPlayer());
        } else {
            inGangChat.add(getPlayer().getUniqueId());
            new ChatBuilder(CmdGang.PREFIX + "You have&a entered &e&lGang Chat" + (ChatColor.getLastColors(CmdGang.PREFIX)) + ".").send(getSpigotPlayer());
        }
    }

    @EventSubscription(priority = EventPriority.LOW)
    private void onChat(AsyncPlayerChatEvent event) {
        if(!inGangChat.contains(event.getPlayer().getUniqueId()))
            return;
        event.setCancelled(true);

        GangMember member = GangMemberManager.instance.getMember(event.getPlayer());
        if (member == null) {
            String id = ModuleErrors.instance.recordError(new IllegalStateException("Player " + event.getPlayer().getName() +
                    " (" + event.getPlayer().getUniqueId().toString() + ") has no corresponding gang member object!"));

            new ChatBuilder("&c&lHey! &7There was a problem with sending your message in your gang's chat,")
                    .addText("please send this error id to an admin: &4&l" + id, HoverUtil.text("&4&l" + id))
                    .addText("\n&7For now, we have&c blocked&7 the message you sent, and&a switched you back to normal chat!").send(getSpigotPlayer());
            return;
        }

        if(member.getGang() == null) {
            inGangChat.remove(event.getPlayer().getUniqueId());
            event.setCancelled(false);
            return;
        }

        Gang gang = GangManager.instance.getLoadedGang(member.getGang());

        ChatBuilder builder = new ChatBuilder();

        builder.addText("&e&lGANG &8[&b&l" + member.getGangRole().toString() + "&8] &6")
                .addText(event.getPlayer().getDisplayName(), HoverUtil.text("&dClick to open their profile"), ClickUtil.command("/profile " + event.getPlayer().getName()))
                .addText(" &8&l▶ &7" + event.getMessage());

        builder.sendAll(gang.getMembers()
                .stream()
                .map((m) -> Bukkit.getPlayer(m.getMember()))
                .collect(Collectors.toList()));
    }
}
