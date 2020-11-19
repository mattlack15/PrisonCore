package com.soraxus.prisons.core;

import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.PluginFile;
import com.soraxus.prisons.util.PluginFolder;
import com.soraxus.prisons.util.menus.MenuElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;

public abstract class CoreModule {
    @Getter
    private boolean enabled;

    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private CorePlugin parent;

    public abstract String getName();

    private FileConfiguration config = null;

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public File getDataFolder() {
        if (parent == null)
            return null;
        File parentFolder = parent.getDataFolder();
        File dataFolder = new File(parentFolder, this.getName());
        dataFolder.mkdirs();
        return dataFolder;
    }

    protected FileConfiguration getConfig() {
        if (config != null)
            return config;
        File file = new File(getDataFolder(), getName() + "_config.yml");
        if (!file.exists()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(file.getName());
            if (stream != null) {
                try {
                    OutputStream outputStream = new FileOutputStream(file);
                    byte[] bites = new byte[stream.available()];
                    stream.read(bites);
                    outputStream.write(bites);
                    outputStream.flush();
                    outputStream.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        return config;
    }

    protected void saveConfig() {
        try {
            this.config.save(new File(getDataFolder(), getName() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void enable() {
        if (this.isEnabled())
            return;
        System.out.println("[SPC] Enabling module: " + getName());
        EventSubscriptions.instance.subscribe(this);
        try {
            this.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.enabled = true;
    }

    public final void disable() {
        if (!this.isEnabled())
            return;
        EventSubscriptions.instance.unSubscribe(this);
        try {
            this.onDisable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.enabled = false;
    }

    public abstract MenuElement getGUI(MenuElement backButton);

    /**
     * Creates all static files annotated with @PluginFile or @PluginFolder in the specified class<br>
     * It is recommended to make the files/folders be in the direction this.getDataFolder()
     */
    public void createFiles(Class<?> fileClass) {
        Field[] fields = fileClass.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                if (f.getType() == File.class) {
                    if (f.getAnnotation(PluginFile.class) != null) {
                        try {
                            Object fil = f.get(null);
                            if (!(fil instanceof File))
                                continue;
                            File file = (File) fil;
                            PluginFile ann = f.getAnnotation(PluginFile.class);
                            if (!file.exists()) {
                                InputStream stream = getClass().getClassLoader().getResourceAsStream(ann.resourcePath() +
                                        (ann.resourcePath().endsWith(File.separator) ? ann.resourcePath() : (ann.resourcePath().isEmpty() ? "" : ann.resourcePath() + File.separator)) + file.getName());
                                if (stream != null) {
                                    try {
                                        OutputStream outputStream = new FileOutputStream(file);
                                        byte[] bites = new byte[stream.available()];
                                        stream.read(bites);
                                        outputStream.write(bites);
                                        outputStream.flush();
                                        outputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    stream.close();
                                } else {
                                    new File(file.getPath().substring(0, file.getPath().lastIndexOf(File.separator))).mkdirs();
                                    file.createNewFile();
                                }
                            }
                        } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
                            e.printStackTrace();
                        }
                    } else if (f.getAnnotation(PluginFolder.class) != null) {
                        try {
                            File file = (File) f.get(null);
                            file.mkdirs();
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }

    protected ExecutorService getAsyncExecutor() {
        return parent.getAsyncExecutor();
    }
}