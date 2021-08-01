package com.soraxus.prisons.gangs.cmd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.soraxus.prisons.gangs.*;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.exception.CommandException;
import net.ultragrav.command.platform.SpigotCommand;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GangCommand extends SpigotCommand {
    @Getter
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setUncaughtExceptionHandler((e, e1) -> e1.printStackTrace()).build());

    @Getter
    @Setter
    private boolean requiresGang = false;

    @Getter
    @Setter
    private GangRole minimumRole = GangRole.values()[0];

    public GangMember getGangMember() {
        return GangMemberManager.instance.getMember(getPlayer().getUniqueId());
    }

    public Gang getGang() {
        if (!isPlayer()) {
            return null;
        }
        UUID id = getGangMember().getGang();
        return GangManager.instance.getLoadedGang(id);
    }

    public void preConditions() {
        if (requiresGang && (getGang() == null || getGangMember() == null)) {
            throw new CommandException("§cThis command requires you to be in a gang!");
        }
        if (minimumRole.ordinal() > getGangMember().getGangRole().ordinal()) {
            throw new CommandException("&cYou are not a high enough role to use this!");
        }
    }

    @Override
    public boolean isAllowConsole() {
        return !requiresGang && super.isAllowConsole(); // Don't allow console if a gang is required
    }
}
