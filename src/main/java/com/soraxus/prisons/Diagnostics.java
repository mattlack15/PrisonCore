package com.soraxus.prisons;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Diagnostics {
    @Getter
    @AllArgsConstructor
    private static class Diagnostic {
        private String name;
        private Runnable run;
    }

    private static final List<Diagnostic> diagnostics = new ArrayList<>();
    static {
        diagnostics.add(new Diagnostic("gang_io", () -> {
            // TODO: Test Gang loading and saving on UUID-0 test gang
        }));
    }

    public static void runDiagnostics() {
        int count = 0;
        int success = 0;
        for (Diagnostic diagnostic : diagnostics) {
            System.out.println("Running diagnostic: " + diagnostic.getName());
            count ++;
            try {
                diagnostic.getRun().run();
                success ++;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Successful diagnostics: " + success + " / " + count);
    }
}
