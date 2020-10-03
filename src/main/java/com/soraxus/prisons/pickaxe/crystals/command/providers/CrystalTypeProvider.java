package com.soraxus.prisons.pickaxe.crystals.command.providers;

import com.soraxus.prisons.pickaxe.crystals.CrystalType;
import lombok.Getter;
import net.ultragrav.command.provider.impl.EnumProvider;

public class CrystalTypeProvider extends EnumProvider<CrystalType> {
    @Getter
    private static CrystalTypeProvider instance = new CrystalTypeProvider();

    private CrystalTypeProvider() {
        super(CrystalType.class);
    }
}
