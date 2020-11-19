package com.soraxus.prisons.bunkers.util;

import com.soraxus.prisons.bunkers.ModuleBunkers;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class BunkerSchematics {
    private static final Map<String, Schematic> loadedSchems = new ConcurrentHashMap<>();

    private static final ReentrantLock lock = new ReentrantLock(true); //REQUIRED

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
        lock.lock(); //REQUIRED: I did a lot of testing and this is required due to heap overflows with concurrent decompression
        try {
            if (loadedSchems.containsKey(name)) {
                return loadedSchems.get(name);
            }
            System.out.println("Loading schematic: " + name);
            try {
                File f = new File(ModuleBunkers.instance.getDataFolder(), "schematics/" + name + ".bschem");
                if (!f.exists()) {
                    Bukkit.getLogger().log(Level.SEVERE, "Missing schematic: " + name);
                    return getDefaultSchematic();
                }
                Schematic ret = new Schematic(f);
                loadedSchems.put(name, ret);
                return ret;
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Missing schematic: " + name);
                return getDefaultSchematic();
            }
        } finally {
            lock.unlock();
        }
    }

    @NotNull
    public static Schematic getDefaultSchematic() {
        return getDefaultSchematic(IntVector2D.ONE);
    }

    @NotNull
    public static Schematic getDefaultSchematic(IntVector2D size) {
        Schematic schem = get("default");
        return schem.stack(size.getX() - 1, 0, size.getY() - 1);
    }
}
