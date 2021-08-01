package com.soraxus.prisons.debug.commands;

import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.Bukkit;

public class DebugChatBuilder extends SpigotCommand {
    public DebugChatBuilder() {
        this.addAlias("chatbuilder");
        addParameter(StringProvider.getInstance(), "text");
    }

    @Override
    protected void perform() {
        String[] blocks = this.<String>getArgument(0).split(";");
        ChatBuilder builder = new ChatBuilder();
        int i = 0;
        for (String s : blocks) {
            builder.addText(s, HoverUtil.text(i++ + ""));
        }
        builder.send(getSpigotPlayer());
        Bukkit.broadcastMessage(builder.build().getColorRaw() + "");
    }
}
