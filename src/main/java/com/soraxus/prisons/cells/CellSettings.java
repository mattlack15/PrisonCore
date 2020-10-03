package com.soraxus.prisons.cells;

public class CellSettings {

    public enum OpenSetting {
        OPEN,
        TRUSTED,
        CLOSED
    }

    public enum ProtectionSetting {
        EVERYONE,
        TRUSTED,
        OWNER
    }

    private final Cell parent;
    public CellSettings(Cell parent) {
        this.parent = parent;
    }

    private <T> T getSetting(String setting, T defaultValue) {
        return this.parent.getMeta().getOrSet("setting." + setting, defaultValue);
    }

    private <T> void setSetting(String setting, T value) {
        this.parent.getMeta().set("setting." + setting, value);
    }

    //Open setting
    public OpenSetting getOpenSetting() {
        return getSetting("open_setting", OpenSetting.OPEN);
    }
    public void setOpenSetting(OpenSetting setting) {
        setSetting("open_setting", setting);
    }

    //World time setting
    public int getWorldTime() {
        return getSetting("world_time", 0);
    }
    public void setWorldTime(int time) {
        setSetting("world_time", time);
    }

    //Protection setting
    public ProtectionSetting getProtectionSetting() {
        return getSetting("protection_setting", ProtectionSetting.TRUSTED);
    }
    public void setProtectionSetting(ProtectionSetting setting) {
        setSetting("protection_setting", setting);
    }
}
