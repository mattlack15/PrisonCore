/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtils {
    /**
     * Load the contents of a file into a string
     *
     * @param f File
     * @return File contents as string
     */
    @Nullable
    public static String loadFile(@NotNull File f) {
        if (!f.exists()) {
            try {
                if (f.getParentFile().mkdirs()) {
                    if (f.createNewFile()) {
                        return "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            StringBuilder str = new StringBuilder();
            while (stream.available() > 0) {
                int i = stream.read();
                str.append((char) i);
            }
            return str.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Write the contents of a String into a file
     *
     * @param f    File
     * @param data Data
     * @return File contents as string
     */
    @Nullable
    public static void writeFile(@NotNull File f, String data) {
        if (!f.exists()) {
            try {
                if (f.getParentFile().mkdirs()) {
                    f.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream stream = new FileOutputStream(f);
            byte[] dat = data.getBytes();
            for (byte ch : dat) {
                stream.write(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Will move the source File to the destination File.
     * The Method will backup the dest File, copy source to
     * dest, and then will delete the source and the backup.
     *
     * @param source File to be moved
     * @param dest   File to be overwritten (does not matter if
     *               non existent)
     * @throws IOException
     */
    public static void moveAndOverwrite(File source, File dest) throws IOException {
        // Backup the src
        File backup = getNonExistingTempFile(dest);
        org.apache.commons.io.FileUtils.copyFile(dest, backup);
        org.apache.commons.io.FileUtils.copyFile(source, dest);
        if (!source.delete()) {
            throw new IOException("Failed to delete " + source.getName());
        }
        if (!backup.delete()) {
            throw new IOException("Failed to delete " + backup.getName());
        }
    }

    /**
     * Recursive Method to generate a FileName in the same
     * Folder as the {@code inputFile}, that is not existing
     * and ends with {@code _temp}.
     *
     * @param inputFile The FileBase to generate a Tempfile
     * @return A non existing File
     */
    public static File getNonExistingTempFile(File inputFile) {
        File tempFile = new File(inputFile.getParentFile(), inputFile.getName() + "_temp");
        if (tempFile.exists()) {
            return getNonExistingTempFile(tempFile);
        } else {
            return tempFile;
        }
    }
}
