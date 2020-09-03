package com.soraxus.prisons.economy;

import com.soraxus.prisons.util.NumberUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultEconomyHook implements Economy {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "SPCEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double v) {
        return NumberUtils.formatFull(v);
    }

    @Override
    public String currencyNamePlural() {
        return "IDKs";
    }

    @Override
    public String currencyNameSingular() {
        return "IDK";
    }

    @Override
    public boolean hasAccount(String s) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return true;
    }

    @Override
    public boolean hasAccount(String player, String world) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {
        return true;
    }

    @Override
    public double getBalance(String s) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(s);
        if (op == null) {
            return 0;
        }
        return getBalance(op);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return com.soraxus.prisons.economy.Economy.money.getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(s);
    }

    @Override
    public double getBalance(OfflinePlayer op, String s) {
        return getBalance(op);
    }

    @Override
    public boolean has(String s, double v) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(s);
        if (op == null) {
            return false;
        }
        return has(op, v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return com.soraxus.prisons.economy.Economy.money.hasBalance(offlinePlayer.getUniqueId(), (long) v);
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(s, v);
    }

    @Override
    public boolean has(OfflinePlayer op, String s, double v) {
        return has(op, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(s);
        if (op == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE,
                    "Invalid player");
        }
        return withdrawPlayer(op, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer op, double v) {
        return new EconomyResponse(v,
                com.soraxus.prisons.economy.Economy.money.removeBalance(op.getUniqueId(), (long) v),
                EconomyResponse.ResponseType.SUCCESS,
                null
        );
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(s, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer op, String s, double v) {
        return withdrawPlayer(op, v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(s);
        if (op == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE,
                    "Invalid player");
        }
        return depositPlayer(op, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer op, double v) {
        return new EconomyResponse(v,
                com.soraxus.prisons.economy.Economy.money.addBalance(op.getUniqueId(), (long) v),
                EconomyResponse.ResponseType.SUCCESS,
                null
        );
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(s, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer op, String s, double v) {
        return depositPlayer(op, v);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer op) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer op, String s) {
        return false;
    }
}
