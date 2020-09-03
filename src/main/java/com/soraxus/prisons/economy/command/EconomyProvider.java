package com.soraxus.prisons.economy.command;

import com.soraxus.prisons.economy.Economy;
import lombok.Getter;
import lombok.NonNull;
import net.ultragrav.command.exception.CommandException;
import net.ultragrav.command.provider.UltraProvider;

import java.util.ArrayList;
import java.util.List;

public class EconomyProvider extends UltraProvider<Economy> {
    @Getter
    private static final EconomyProvider instance = new EconomyProvider();

    private EconomyProvider() {}

    @Override
    public Economy convert(@NonNull String s) throws CommandException {
        if (!Economy.economies.containsKey(s.toLowerCase())) {
            throw new CommandException("§cEconomy not found: " + s);
        }
        return Economy.economies.get(s.toLowerCase());
    }

    @Override
    public List<String> tabComplete(@NonNull String s) {
        List<String> ret = new ArrayList<>();
        for (String str : Economy.economies.keySet()) {
            if (str.toLowerCase().startsWith(s)) {
                ret.add(str);
            }
        }
        return ret;
    }

    @Override
    public String getArgumentDescription() {
        return "economy";
    }
}
