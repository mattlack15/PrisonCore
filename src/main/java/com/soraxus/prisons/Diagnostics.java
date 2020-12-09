package com.soraxus.prisons;

import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Diagnostics {
    private static final UUID zeroId = new UUID(0, 0);

    @Getter
    @AllArgsConstructor
    private static class Diagnostic {
        private String name;
        private Runnable run;
    }

    private static final List<Diagnostic> diagnostics = new ArrayList<>();

    static {
        diagnostics.add(new Diagnostic("gang_io", () -> {
            GangManager gangManager = GangManager.instance;
            Gang gang;
            if (!gangManager.gangExists(zeroId)) {
                gang = gangManager.createGang("__null__");
                gang.createBunkerAsync().join();
            } else {
                gang = gangManager.getOrLoadGang(zeroId);
            }

            gangManager.saveGang(gang);
            gang.disband();
        }));
    }

    public static void runDiagnostics() {
        new Thread(() -> {
            int count = 0;
            int success = 0;
            for (Diagnostic diagnostic : diagnostics) {
                System.out.println("Running diagnostic: " + diagnostic.getName());
                count++;
                try {
                    diagnostic.getRun().run();
                    success++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Successful diagnostics: " + success + " / " + count);
        }).start();
    }
}
