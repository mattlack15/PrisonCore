/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.util;

import com.soraxus.prisons.util.math.BigMath;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {
    @NotNull
    public static String convertTime(long seconds) {
        long temp = seconds;
        long hours = Math.floorDiv(temp, 3600);
        temp -= hours * 3600;
        long minutes = Math.floorDiv(temp, 60);
        temp -= minutes * 60;
        long secs = temp;

        String secos = String.format("%02d", secs);
        if (hours == 0) {
            return minutes + ":" + secos;
        } else {
            String mins = String.format("%02d", minutes);
            return hours + ":" + mins + ":" + secos;
        }
    }

    @NotNull
    public static String convertTime(@NotNull BigInteger seconds) {
        BigInteger[] temps = seconds.divideAndRemainder(BigInteger.valueOf(60));
        BigInteger minutes = temps[0];
        BigInteger secs = temps[1];
        temps = minutes.divideAndRemainder(BigInteger.valueOf(60));
        minutes = temps[1];
        BigInteger hours = temps[0];

        String secos = String.format("%02d", secs);
        String mins = String.format("%02d", minutes);
        String hors = String.format("%02d", hours);
        return hors + ":" + mins + ":" + secos;
    }

    /**
     * Convert from milliseconds to readable format
     *
     * @param millis Milliseconds
     * @return Readable
     */
    @NotNull
    public static String convertTimeM(@NotNull BigInteger millis) {
        return convertTime(millis.divide(BigMath.THOUSAND));
    }

    @NotNull
    public static String convertTimeM(long millis) {
        return convertTime(millis / 1000);
    }

    private static String plural(int count, String singular) {
        return count + " " + singular + (count == 1 ? "" : "s");
    }

    public static String readableDate(long secs, boolean sh) {
        return readableDate(BigInteger.valueOf(secs), sh);
    }

    public static String readableDate(BigInteger seconds, boolean sh) {
        List<String> ret = new ArrayList<>();
        int years = seconds.divide(BigInteger.valueOf(60 * 60 * 24 * 30 * 12)).intValue();
        seconds = seconds.mod(BigInteger.valueOf(60 * 60 * 24 * 30 * 12));
        int months = seconds.divide(BigInteger.valueOf(60 * 60 * 24 * 30)).intValue();
        seconds = seconds.mod(BigInteger.valueOf(60 * 60 * 24 * 30));
        int days = seconds.divide(BigInteger.valueOf(60 * 60 * 24)).intValue();
        seconds = seconds.mod(BigInteger.valueOf(60 * 60 * 24));
        int hours = seconds.divide(BigInteger.valueOf(60 * 60)).intValue();
        seconds = seconds.mod(BigInteger.valueOf(60 * 60));
        int minutes = seconds.divide(BigInteger.valueOf(60)).intValue();
        seconds = seconds.mod(BigInteger.valueOf(60));
        int secs = seconds.intValue();

        if (years > 0) {
            ret.add(sh ? years + "Y" : plural(years, "year"));
        }
        if (months > 0) {
            ret.add(sh ? months + "M" : plural(months, "month"));
        }
        if (days > 0) {
            ret.add(sh ? days + "D" : plural(days, "day"));
        }
        if (hours > 0) {
            ret.add(sh ? hours + "h" : plural(hours, "hour"));
        }
        if (minutes > 0) {
            ret.add(sh ? minutes + "m" : plural(minutes, "minute"));
        }
        if (secs > 0) {
            ret.add(sh ? secs + "s" : plural(secs, "second"));
        }
        return String.join(sh ? " " : ", ", ret);
    }
}