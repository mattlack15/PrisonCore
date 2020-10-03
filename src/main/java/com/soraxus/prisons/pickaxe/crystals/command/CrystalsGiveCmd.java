package com.soraxus.prisons.pickaxe.crystals.command;

import com.soraxus.prisons.pickaxe.crystals.Crystal;
import com.soraxus.prisons.pickaxe.crystals.CrystalType;
import com.soraxus.prisons.pickaxe.crystals.command.providers.CrystalTypeProvider;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.IntegerProvider;
import net.ultragrav.command.provider.impl.spigot.PlayerProvider;
import org.bukkit.entity.Player;

public class CrystalsGiveCmd extends UltraCommand {
    public CrystalsGiveCmd() {
        this.addAlias("give");

        this.addParameter(PlayerProvider.getInstance());
        this.addParameter(CrystalTypeProvider.getInstance());
        this.addParameter(IntegerProvider.getInstance(), "tier");
    }

    @Override
    protected void perform() {
        Player player = getArgument(0);
        CrystalType type = getArgument(1);
        int tier = getArgument(2);

        Crystal crystal = new Crystal(-1, type, tier);
        player.getInventory().addItem(crystal.getItem());
        tell("§aGave " + player.getName() + " a tier " + tier + " " + type.getDisplayName() + " crystal!");
    }
}
