package com.soraxus.prisons.core;

import com.soraxus.prisons.core.command.CmdCorePlugin;
import com.soraxus.prisons.core.command.GravSubCommand;
import com.soraxus.prisons.util.PluginFile;
import com.soraxus.prisons.util.PluginFolder;
import com.soraxus.prisons.util.data.PlayerData;
import com.soraxus.prisons.util.menus.Menu;
import lombok.Getter;
import net.ultragrav.command.UltraCommand;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class CorePlugin extends JavaPlugin {
    private List<CoreModule> modules = new ArrayList<>();

    private CmdCorePlugin command;
    private String commandName;
    private String commandPrefix;

    @Getter
    private ExecutorService asyncExecutor;

    @Getter
    private JavaPlugin plugin;

    public CorePlugin(String commandName, String commandPrefix) {
        this.commandName = commandName;
        this.commandPrefix = commandPrefix;
        this.plugin = this;
        this.asyncExecutor = Executors.newCachedThreadPool();
    }

    protected void init() {
        PlayerData.init();
        command = new CmdCorePlugin(commandName, ChatColor.translateAlternateColorCodes('&', commandPrefix));
    }

    public void addSubCommand(UltraCommand command) {
        this.command.addChildren(command);
    }

    /**
     * Adds and enables a module
     */
    public synchronized void addModule(CoreModule module) {
        module.setParent(this);
        this.modules.add(module);
        module.enable();
    }

    /**
     * Get the GUI of the
     *
     * @return Menu
     */
    public Menu getCoreGUI() {
        return new CoreGUI(this.getName(), this.modules);
    }

    /**
     * Get a copy of the module list
     */
    public synchronized List<CoreModule> getModules() {
        return new ArrayList<>(modules);
    }

    /**
     * Disable and remove a module by name
     */
    public synchronized void removeModule(String name) {
        this.modules.removeIf(m -> {
            if (m.getName().equals(name)) {
                m.disable();
                m.setParent(null);
                return true;
            }
            return false;
        });
    }

    /**
     * Disable and remove all modules
     */
    public synchronized void clearModules() {
        this.getModules().forEach(m -> removeModule(m.getName()));
    }

    public synchronized <T extends CoreModule> T getModule(Class<T> clazz) {
        for (CoreModule module : this.modules) {
            if (module.getClass().equals(clazz)) {
                return clazz.cast(module);
            }
        }
        return null;
    }

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
}


