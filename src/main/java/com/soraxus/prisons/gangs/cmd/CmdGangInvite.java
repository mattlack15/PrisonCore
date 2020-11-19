package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.entity.Player;

import static com.soraxus.prisons.gangs.cmd.CmdGang.PREFIX;

public class CmdGangInvite extends GangCommand {
    public CmdGangInvite() {
        this.addAlias("invite");

        this.setRequiresGang(true);

        this.addParameter(PlayerProvider.getInstance());
    }

    public void perform() {
        Player player = getArgument(0);

        getAsyncExecutor().submit(() -> {
            Gang gang = getGang();
            GangMember member = getGangMember();
            if (!member.getGangRole().isCanInvite()) {
                tell(PREFIX + "&cYour current role cannot invite players to your gang!");
                return;
            }
            if (gang.isInvited(player.getUniqueId())) {
                tell(PREFIX + "&cPlayer is already invited!");
                return;
            }
            gang.invite(player.getUniqueId(), player.getName());
            gang.broadcastMessage("&e" + sender.getName() + " &7invited &9" + player.getName() + " &7to the gang!");
            new ChatBuilder()
                    .addText(
                            PREFIX + "You were invited to " + gang.getName() + "&a&l Click to join!",
                            HoverUtil.text("&aClick to join this gang!"),
                            ClickUtil.command("/gang join " + gang.getName())
                    )
                    .send(player);
        });
    }
}
