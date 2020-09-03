package com.soraxus.prisons.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

public class ArrayUtils {
    public static String toString(int[][][] arr) {
        StringBuilder str = new StringBuilder("[");
        for (int[][] i : arr) {
            str.append(toString(i)).append(",");
        }
        str.append("]");
        return str.toString();
    }

    public static String toString(int[][] arr) {
        StringBuilder str = new StringBuilder("[");
        for (int[] i : arr) {
            str.append(toString(i)).append(",");
        }
        str.append("]");
        return str.toString();
    }

    public static String toString(int[] arr) {
        StringBuilder str = new StringBuilder("[");
        for (int i : arr) {
            str.append(i).append(",");
        }
        str.append("]");
        return str.toString();
    }

    public static Object[] arrayToObject(int[][][] in) {
        return Arrays.stream(in).toArray();
    }

    public static <I> Object[] arrayToObject(I[] in) {
        return Arrays.stream(in).toArray();
    }

    public static <T> T[] castArray(Object[] in, Class<T> clazz, IntFunction<T[]> arrSupplier) {
        return Arrays.stream(in)
                .map(clazz::cast)
                .toArray(arrSupplier);
    }

    public static int[][][] castArrayToTripleInt(Object[] in) {
        return Arrays.stream(in)
                .map(obj -> castArrayToDoubleInt((Object[]) obj))
                .toArray(int[][][]::new);
    }

    public static int[][] castArrayToDoubleInt(Object[] in) {
        return Arrays.stream(in)
                .map(int[].class::cast)
                .toArray(int[][]::new);
    }

    //    public static <T> T[] arrayFromObj(Object in) {
//
//    }
    public static void shift(boolean[] in) {
        boolean temp = in[in.length - 1];
        System.arraycopy(in, 0, in, 1, in.length - 2 + 1);
        in[0] = temp;
    }

    public static void shift(int[] in) {
        int temp = in[in.length - 1];
        System.arraycopy(in, 0, in, 1, in.length - 2 + 1);
        in[0] = temp;
    }

    public static <T> void shift(T[] in) {
        T temp = in[in.length - 1];
        System.arraycopy(in, 0, in, 1, in.length - 2 + 1);
        in[0] = temp;
    }

    public static boolean equals(boolean[] one, boolean[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(int[] one, int[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean equals(T[] one, T[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (!Objects.equals(one[i], two[i])) {
                return false;
            }
        }
        return true;
    }
}
