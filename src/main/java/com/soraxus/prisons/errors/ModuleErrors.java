package com.soraxus.prisons.errors;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ModuleErrors extends CoreModule {

    public static ModuleErrors instance;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Random random = new Random(System.currentTimeMillis());

    @Override
    protected void onEnable() {
        instance = this;
        new CmdErrorTest().register();
    }

    @Override
    protected void onDisable() {
        service.shutdown();
    }

    public ChatBuilder getErrorMessage(Throwable t, String failedAction, String response) {
        String id = recordError(t, failedAction, response, true);
        return new ChatBuilder("&c&lError &8&l▶ &fSorry! There was a problem &e" + failedAction + "&f, we've &a" + response + "&f.")
                .addText("\nPlease send this error id to an admin: &4&l" + id, HoverUtil.text("&4&l" + id));
    }

    /**
     * Records error to a file and returns an identifier.
     */
    public String recordError(Throwable t) {
        return recordError(t, "Unknown action", "No identified response", true);
    }

    public String recordError(Throwable t, String failedAction, String response) {
        return recordError(t, failedAction, response, true);
    }

    public String recordError(Throwable t, String failedAction, String response, boolean verbose) {
        String identifier = getIdentifier(10);
        service.submit(() -> {
            File file = new File(this.getDataFolder(), identifier + ".txt");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write("Failed Action: " + failedAction + "\n");
                fileWriter.write("Response: " + response + "\n\n");
                fileWriter.write(t.toString());
                t.printStackTrace();
                for (StackTraceElement element : t.getStackTrace()) {
                    fileWriter.write(element.toString() + "\n");
                }
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (verbose)
            new ChatBuilder("&c&lERROR &8&l▶ &fAn error was recorded with ID: &4&l" + identifier)
                    .addText("\n&fThe server has failed to &c" + failedAction + "&f and has in response, &a" + response + "&f.")
                    .sendAll(Bukkit.getOnlinePlayers()
                            .stream()
                            .filter((p) -> p.hasPermission("spc.error.verbose"))
                            .collect(Collectors.toList()));

        return identifier;
    }

    private synchronized String getIdentifier(int length) {
        long val = random.nextLong();
        StringBuilder m = new StringBuilder(Long.toHexString(val));
        if (m.length() > length) {
            m = new StringBuilder(m.substring(m.length() - length));
        }
        while (m.length() < length) {
            int i = random.nextInt();
            String o = Integer.toHexString(i);
            m.insert(0, o.charAt(o.length() - 1));
        }
        return m.toString();
    }

    @Override
    public String getName() {
        return "Errors";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }
}
