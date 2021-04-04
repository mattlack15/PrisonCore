package com.soraxus.prisons.shop;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.shop.customshop.CustomShop;
import com.soraxus.prisons.shop.customshop.menu.MenuCustomShop;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.MenuElement;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Material;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ModuleShop extends CoreModule {

    private static ModuleShop instance;

    public static ModuleShop getInstance() {
        return instance;
    }

    private volatile static CustomShop globalShop;

    public static CustomShop getGlobalShop() {
        return globalShop;
    }

    private File shopFile;

    @Override
    protected void onEnable() {
        instance = this;

        shopFile = new File(getDataFolder(), "global.shop");

        try {
            if (shopFile.createNewFile()) {
                globalShop = new CustomShop("Global");
            } else {
                loadShop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDisable() {
        saveShop();
    }

    private synchronized void loadShop() {
        try {
            GravSerializer serializer = new GravSerializer(new FileInputStream(shopFile));
            globalShop = new CustomShop(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveShop() {
        GravSerializer serializer = new GravSerializer();
        globalShop.serialize(serializer);
        try {
            serializer.writeToStream(new FileOutputStream(shopFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Shops";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.CHEST)
                .setName("&f&lGlobal Shop")
                .addLore("&7Click to enter &f&lGlobal Shop")
                .build())
                .clickBuilder()
                .onLeftClick((e) -> new MenuCustomShop(globalShop, e.getWhoClicked().hasPermission("globalshop.edit")).open(e.getWhoClicked()))
                .build();
    }
}
