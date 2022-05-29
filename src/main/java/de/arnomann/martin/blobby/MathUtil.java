package de.arnomann.martin.blobby;

public class MathUtil {

    private MathUtil() {}

    public static boolean inclusiveBetween(double min, double max, double val) {
        return Math.max(min, val) == Math.min(max, val);
    }

    public static boolean exclusiveBetween(double min, double max, double val) {
        return min < val && val < max;
    }

}
