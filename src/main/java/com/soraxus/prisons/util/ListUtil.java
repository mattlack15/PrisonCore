package com.soraxus.prisons.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ListUtil {
    @NotNull
    @Contract("_ -> new")
    public static <T> List<T> clone(List<T> in) {
        return new ArrayList<T>(in);
    }

    public static <T> T randomElement(List<T> in) {
        if (in.size() == 0) {
            return null;
        }
        return in.get(ThreadLocalRandom.current().nextInt(in.size()));
    }
}
