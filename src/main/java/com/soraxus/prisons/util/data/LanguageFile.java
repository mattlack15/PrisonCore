package com.soraxus.prisons.util.data;

import java.io.File;
import java.util.Map;

public abstract class LanguageFile {
    private File file;
    private Map<String, String> defaults;
    private Map<String, String> loaded;

    public LanguageFile(File file) {
        this.file = file;
        this.defaults = defaults();
        String[] lines = FileUtils.loadFile(file).split("\n");
        for (String str : lines) {
            int i = str.indexOf(":");
            String key = str.substring(0, i);
            String value = str.substring(i + 1).trim();
            loaded.put(key, value);
        }
    }

    public void save() {

    }



    public abstract Map<String, String> defaults();
}
