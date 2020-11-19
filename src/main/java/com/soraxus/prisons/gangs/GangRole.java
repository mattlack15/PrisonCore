package com.soraxus.prisons.gangs;

import lombok.Getter;

public enum GangRole {
    RECRUIT(true, false),
    MEMBER(true, false),
    MOD(true, false),
    ADMIN(true, true),
    LEADER(false, true);

    @Getter
    private boolean duplicatable;

    @Getter
    private boolean canInvite;

    GangRole(boolean duplicatable, boolean canInvite) {
        this.duplicatable = duplicatable;
        this.canInvite = canInvite;
    }

    private static GangRole[] values = null;

    public static GangRole[] getValues() {
        if (values == null) {
            values = values();
        }
        return values;
    }
}
