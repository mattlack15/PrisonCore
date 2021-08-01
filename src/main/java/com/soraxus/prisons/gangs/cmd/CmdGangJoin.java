package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangJoin extends GangCommand {
    public CmdGangJoin() {
        this.addAlias("join");

        this.setAllowConsole(false);

        this.addParameter(GangProvider.getInstance(), "gang");
    }

    public void perform() {
        getAsyncExecutor().submit(() -> {
            try {
                Gang gang = getArgument(0);
                if (getGangMember().getGang() != null && getGangMember().getGang().equals(gang.getId())) {
                    new ChatBuilder(PREFIX + "You are already in this gang...?").send(getSpigotPlayer());
                    return;
                }
                if (!gang.addMemberWithCondition(getGangMember(), () -> {
                    if (gang.isInvited(getGangMember().getMember())) {
                        gang.unInvite(getGangMember().getMember());
                        return true;
                    } else return sender.hasPermission("gang.admin");
                })) {
                    tell(PREFIX + ChatColor.RED + "You are not invited to this gang!");
                    return;
                }
                gang.broadcastMessage("&a" + getSpigotPlayer() + "&f joined the gang!");
            } catch(CommandException e) {
                new ChatBuilder(e.getMessage()).send(getSpigotPlayer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
