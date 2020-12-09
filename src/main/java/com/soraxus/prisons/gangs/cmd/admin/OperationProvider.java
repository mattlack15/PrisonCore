package com.soraxus.prisons.gangs.cmd.admin;

import lombok.Getter;
import net.ultragrav.command.provider.impl.EnumProvider;

public class OperationProvider extends EnumProvider<XpOperation> {
    @Getter
    private static OperationProvider instance = new OperationProvider();

    private OperationProvider() {
        super(XpOperation.class);
    }
}