package de.arnomann.martin.blobby;

/**
 * A utility class for maths.
 */
public class MathUtil {

    private MathUtil() {}

    /**
     * Checks if a value between two other values. Will also return {@code true} if the value is {@code min} or {@code max}.
     * @param min the minimum value.
     * @param max the maximum value.
     * @param val the value to check.
     * @return {@code true} if the value is inclusively between {@code min} and {@code max}, {@code false} otherwise.
     */
    public static boolean inclusiveBetween(double min, double max, double val) {
        return Math.max(min, val) == Math.min(max, val);
    }

    /**
     * Checks if a value between two other values. Will not return {@code true} if the value is {@code min} or {@code max}.
     * @param min the minimum value.
     * @param max the maximum value.
     * @param val the value to check.
     * @return {@code true} if the value is exclusively between {@code min} and {@code max}, {@code false} otherwise.
     */
    public static boolean exclusiveBetween(double min, double max, double val) {
        return min < val && val < max;
    }

    /**
     * Converts a boolean to an integer.
     * @param b the boolean to convert.
     * @return {@code 1} if {@code b} is {@code true}, {@code 0} otherwise.
     */
    public static int booleanToInt(boolean b) {
        return b ? 1 : 0;
    }

}
