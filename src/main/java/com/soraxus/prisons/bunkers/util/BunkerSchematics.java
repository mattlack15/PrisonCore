package com.soraxus.prisons.bunkers.util;

import com.soraxus.prisons.bunkers.ModuleBunkers;
import net.ultragrav.asyncworld.schematics.Schematic;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BunkerSchematics {
    private static final Map<String, Schematic> loadedSchems = new HashMap<>();

//    static {
//        File f = new File(ModuleBunkers.instance.getDataFolder(), "schematics/maps");
//        GravSerializable.a.set(false);
//        for(File file : f.listFiles()) {
//            try {
//                Schematic s = new Schematic(file);
//                s.save(file);
//                Bukkit.broadcastMessage("Updated file " + file.getName());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        GravSerializable.a.set(true);
//    }

    public static void clear() {
        loadedSchems.clear();
    }

    @NotNull
    public static Schematic get(String name) {
        if (loadedSchems.containsKey(name)) {
            return loadedSchems.get(name);
        }
        try {
            File f = new File(ModuleBunkers.instance.getDataFolder(), "schematics/" + name + ".bschem");
            if (!f.exists()) {
                throw new RuntimeException("Missing schematic: " + name);
            }
            Schematic ret = new Schematic(f);
            loadedSchems.put(name, ret);
            return ret;
        } catch(IOException e) {
            throw new RuntimeException("Missing schematic: " + name);
        }
    }
}
