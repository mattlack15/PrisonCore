/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util.math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
    private static Random rand = ThreadLocalRandom.current();

    public static int random(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public static double random(double min, double max) {
        return rand.nextDouble() * (max - min) + min;
    }

    public static int roundRand(double i) {
        int am = (int) Math.floor(i);
        double ret = i - am;
        if (ret >= random(0.0, 1.0)) {
            am++;
        }
        return am;
    }

    public static boolean isRandom(double i, double max) {
        return (i >= random(0.0, max));
    }

    /**
     * Do not touch
     * Only use this method if you understand what it does!
     *
     * @param min    Minimum value
     * @param max    Maximum value
     * @param weight Weight strength
     * @return Value
     */
    public static double weightedRandom(double min, double max, int weight) {
        double num = min;
        double nmax = max - min;
        for (int i = 0; i < weight; i++) {
            num += Math.random() * (nmax / weight);
        }
        return num;
    }

    /**
     * Get a weighted random integer between two values
     *
     * @param min    Minimum value
     * @param max    Maximum value
     * @param weight Weight
     * @return Weighted random number
     */
    public static int weightedRandomI(int min, int max, int weight) {
        return (int) Math.floor(weightedRandom(min, max, weight));
    }

    /**
     * Round a value to a certain amount of decimal places
     *
     * @param val    Number
     * @param places Decimal places
     * @return Rounded number
     */
    public static double round(double val, int places) {
        double qq = Math.pow(10, places);
        return Math.round(val * qq) / qq;
    }

    public static int clampLoop(int i, int min, int max) {
        if (i < min) {
            return clampLoop(i + 1 + max - min, min, max);
        }
        if (i > max) {
            return clampLoop(i - 1 - max + min, min, max);
        }
        return i;
    }

    public static int lowWeightedInt(int min, int max, double weight) {
        return (int) (weightedDoubleInternal(weight) * (max - min) + min);
    }

    public static int highWeightedInt(int min, int max, double weight) {
        return (int) ((1 - weightedDoubleInternal(weight)) * (max - min) + min);
    }

    private static double weightedDoubleInternal(double weight) {
        double d = rand.nextDouble();
        return Math.pow(d, weight + 1);
    }
}
