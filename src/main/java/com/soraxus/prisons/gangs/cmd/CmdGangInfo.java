package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangLevelUtil;
import com.soraxus.prisons.gangs.GangRole;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.string.TextUtil;
import org.bukkit.ChatColor;

import java.util.stream.Collectors;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangInfo extends GangCommand {
    public CmdGangInfo() {
        this.addAlias("info");
        this.addParameter(null, GangProvider.getInstance(), "gang");
    }

    public void perform() {
        Gang gang = getArgument(0);
        if (gang == null) {
            gang = getGang();
            if (gang == null) {
                returnTell(PREFIX + "§cYou are not in a gang, try /gang info <Gang>");
            }
        }
        Gang finalGang = gang;
        getAsyncExecutor().execute(() -> {
            try {
                tell(PREFIX + ChatColor.BLUE + "Name > &f" + finalGang.getName());
                tell(PREFIX + ChatColor.BLUE + "Description > &f" + finalGang.getDescription());
                int level = finalGang.getLevel();
                long xp = finalGang.getXp();
                long reqXp = GangLevelUtil.getReqXp(level);
                String bar = TextUtil.generateBar('d', 'f', '|', 10, xp, reqXp);
                ChatBuilder builder = new ChatBuilder();
                builder.addText(PREFIX + ChatColor.BLUE + "Level > &d" + finalGang.getLevel() + " &8[" + bar + "&8]",
                        HoverUtil.text("&fYour &e&lGang level &fis something\n" +
                                "that determines a number of things including\n" +
                                "whether or not you can have a gang mine, as well\n" +
                                "as how many people can be there.\n" +
                                "\n" +
                                "&aYou can level up your gang by mining."));
                tell(PREFIX + ChatColor.BLUE + "Level > &d" + finalGang.getLevel() + " &8[" + bar + "&8]");
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
