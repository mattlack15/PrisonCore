package com.soraxus.prisons.errors;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.util.menus.MenuElement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleErrors extends CoreModule {

    public static ModuleErrors instance;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Random random = new Random(System.currentTimeMillis());

    @Override
    protected void onEnable() {
        instance = this;
    }

    /**
     * Records error to a file and returns an identifier.
     */
    public String recordError(Throwable t) {
        String identifier = getIdentifier(10);
        service.submit(() -> {
            File file = new File(this.getDataFolder(), identifier + ".txt");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                for (StackTraceElement element : t.getStackTrace()) {
                    fileWriter.write(element.toString() + "\n");
                }
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
