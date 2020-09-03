package com.soraxus.prisons.mines;

import com.soraxus.prisons.util.PluginFile;

import java.io.File;

public class MineFiles {
    @PluginFile
    public static final File MINES_FILE = new File(ModuleMines.instance.getDataFolder(), "mines.yml");
}
