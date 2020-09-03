//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.soraxus.prisons.pluginhooks;

import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.mines.manager.MineManager;
import com.soraxus.prisons.mines.object.Mine;
import com.soraxus.prisons.util.NumberUtils;
import java.math.BigInteger;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

public class SPlaceholderHook extends PlaceholderHook {
    public SPlaceholderHook() {
    }

    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) {
            return "<--->";
        } else {
            if (identifier.startsWith("econ_")) {
                String str = identifier.substring(identifier.indexOf("econ_") + 5);
                if (str.equalsIgnoreCase("stars")) {
                    return NumberUtils.toReadableNumber(BigInteger.valueOf(Economy.stars.getBalance(p.getUniqueId())));
                }

                if (str.equalsIgnoreCase("tokens")) {
                    return NumberUtils.toReadableNumber(BigInteger.valueOf(Economy.tokens.getBalance(p.getUniqueId())));
                }

                if (str.equalsIgnoreCase("balance") || str.equalsIgnoreCase("money")) {
                    return NumberUtils.toReadableNumber(BigInteger.valueOf(Economy.money.getBalance(p.getUniqueId())));
                }
            } else if (identifier.equalsIgnoreCase("currentmine")) {
                Mine mine = MineManager.instance.getMineOf(p.getLocation());
                return mine != null ? mine.getName() : "none";
            }

            return null;
        }
    }
}
