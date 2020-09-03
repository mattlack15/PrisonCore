package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMember;
import com.soraxus.prisons.gangs.GangMemberManager;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.exception.CommandException;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GangCommand extends UltraCommand {
    @Getter
    private ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    @Getter
    @Setter
    private boolean requiresGang = false;

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
    }

    @Override
    public boolean isAllowConsole() {
        return !requiresGang && super.isAllowConsole(); // Don't allow console if a gang is required
    }
}
