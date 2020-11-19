package com.soraxus.prisons.util.list;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ListUtil {
    /**
     * Clone a list
     *
     * @param in  List
     * @param <T> Type of list
     * @return Copy of in
     */
    @NotNull
    @Contract("_ -> new")
    public static <T> List<T> clone(List<T> in) {
        return new ArrayList<T>(in);
    }

    /**
     * Get a random element in the list
     *
     * @param in  List
     * @param <T> Type of list
     * @return Random element
     */
    public static <T> T randomElement(List<T> in) {
        if (in.size() == 0) {
            return null;
        }
        return in.get(ThreadLocalRandom.current().nextInt(in.size()));
    }

    /**
     * Convert a list to a string
     *
     * @param list List
     * @param <T>  Type of list
     * @return String
     */
    public static <T> String toString(List<T> list) {
        StringBuilder str = new StringBuilder("List{");
        for (T t : list) {
            if (t == null) {
                str.append("null");
            } else {
                str.append(t.toString());
            }
        }
        str.append("}");
        return str.toString();
    }

    /**
     * Create a list of length count filled with el
     *
     * @param el    Value
     * @param count Length of list
     * @param <T>   Type of list
     * @return List of length count
     */
    public static <T> List<T> filledList(T el, int count) {
        List<T> ret = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ret.add(el);
        }
        return ret;
    }

    /**
     * Fill a list up to length of count
     *
     * @param list  List
     * @param el    Object to fill with
     * @param count Length
     * @param <T>   Type of list
     */
    public static <T> void fillList(List<T> list, T el, int count) {
        while (list.size() < count) {
            list.add(el);
        }
    }
}
