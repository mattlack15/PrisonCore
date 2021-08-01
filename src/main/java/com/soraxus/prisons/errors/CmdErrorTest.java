package com.soraxus.prisons.errors;

import com.soraxus.prisons.util.display.chat.ChatBuilder;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.StringProvider;

public class CmdErrorTest extends SpigotCommand {
    public CmdErrorTest() {
        this.addAlias("testerror");
        this.addParameter(StringProvider.getInstance(), "message", "test error");
    }

    @Override
    protected void perform() {
        ChatBuilder error = ModuleErrors.instance.getErrorMessage(new IllegalStateException(this.<String>getArgument(0)), getArgument(0), "no response");
        error.send(getSpigotPlayer());
    }
}
