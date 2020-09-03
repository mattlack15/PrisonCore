package com.soraxus.prisons.gangs.cmd;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import lombok.Getter;
import lombok.NonNull;
import net.ultragrav.command.exception.CommandException;
import net.ultragrav.command.provider.UltraProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GangProvider extends UltraProvider<Gang> {
    @Getter
    private static final GangProvider instance = new GangProvider();

    private GangProvider() {}

    @Override
    public Gang convert(@NonNull String s) throws CommandException {
        UUID gangId = GangManager.instance.getId(s);
        if (gangId == null) {
            throw new CommandException("No gang found with name: " + s); // TODO: Similar names with Similarity.getMostSimilar?
        }
        return GangManager.instance.getLoadedGang(gangId);
    }

    @Override
    public List<String> tabComplete(@NonNull String s) {
        List<String> ret = new ArrayList<>();
        for (String str : GangManager.instance.listGangs()) {
            if (str.toLowerCase().startsWith(s)) {
                ret.add(str);
            }
        }
        return ret;
    }

    @Override
    public String getArgumentDescription() {
        return "gang";
    }
}
