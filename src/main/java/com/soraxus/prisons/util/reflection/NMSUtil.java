package com.soraxus.prisons.util.reflection;

import com.soraxus.prisons.util.reflection.nms.NMSObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NMSUtil {
    @Getter
    private static String nmsVersion;

    static {
        nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);
    }

    public static NMSObject wrap(Object obj) {
        return new NMSObject(obj);
    }

    public static NMSObject create(Class<?> clazz, Object... args) {
        try {
            return wrap(clazz.getConstructor(getClasses(args)).newInstance(args));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getNMSClass(String clazz) {
        try {
            return Class.forName(clazz.replaceAll("\\{ver}", nmsVersion));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NMSObject getNMSPlayer(Player player) {
        return wrap(player)
                .cast("org.bukkit.craftbukkit.{ver}.entity.CraftPlayer")
                .invoke("getHandle");
    }

    public static Class<?>[] getClasses(Object[] arr) {
        Class<?>[] ret = new Class[arr.length];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                ret[i] = null;
            } else {
                ret[i] = arr[i].getClass();
            }
        }
        return ret;
    }
}
