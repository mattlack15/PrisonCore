package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.*;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.string.TextUtil;
import java.util.UUID;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangInfo extends GangCommand {
    public CmdGangInfo() {
        this.addAlias("info");
        this.addParameter(null, StringProvider.getInstance(), "gang/player");
    }

    public void perform() {
        String gangName = getArgument(0);
        Gang gang = null;
        if(gangName != null) {
            gang = GangManager.instance.getLoadedGang(gangName);
            if (gang == null) {
                UUID id = GangManager.instance.getId(gangName);
                if (id != null) {
                    gang = GangManager.instance.getOrLoadGang(id);
                }
            }
        }
        if (gang == null) {
            Player player = Bukkit.getPlayer(gangName != null ? gangName : "");
            if (player == null) {
                tell(PREFIX + "&cCould not find player!");
                return;
            }
            gang = GangManager.instance.getLoadedGang(GangMemberManager.instance.getMember(player.getUniqueId()).getGang());
            if (gang == null) {
                tell(PREFIX + "&cPlayer isn't in a gang!");
                return;
            }
        }
        if (gangName == null) {
            gang = getGang();
            if (gang == null) {
                returnTell(PREFIX + "§cYou are not in a gang, try /gang info <gang/player>");
            }
        }
        Gang finalGang = gang;
        getAsyncExecutor().execute(() -> {
            try {
                tell(PREFIX + ChatColor.BLUE + "Name > &f" + finalGang.getName());
                tell(PREFIX + ChatColor.BLUE + "Description > &f" + finalGang.getDescription());
                ChatBuilder builder = new ChatBuilder();
                builder.addText(PREFIX + ChatColor.BLUE + "Level > &d" + finalGang.getLevelInt(),
                        HoverUtil.text("&fYour &e&lGang level &fis something\n" +
                                "that determines a number of things including\n" +
                                "whether or not you can have a gang mine, as well\n" +
                                "as how many people can be there.\n" +
                                "\n" +
                                "&aYou can level up your gang by mining."));
                builder.send(getPlayer());
                tell(PREFIX + ChatColor.BLUE + "Members > &f" + finalGang.getMembers().stream()
                        .map((gm) -> {
                            if (gm.getGangRole().equals(GangRole.LEADER)) {
                                return "**" + gm.getMemberName();
                            } else if (gm.getGangRole().equals(GangRole.ADMIN)) {
                                return "*" + gm.getMemberName();
                            }
                            return gm.getMemberName();
                        })
                        .collect(Collectors.joining(", "))
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
