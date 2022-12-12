package de.arnomann.martin.blobby;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

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
        return min <= val && val <= max;
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

    /**
     * Scales a number from one area to another area.
     * @param inMin the minimum value of the input.
     * @param inMax the maximum value of the input.
     * @param outMin the minimum value of the output.
     * @param outMax the maximum value of the output.
     * @param value the value to scale.
     * @return the scaled value.
     */
    public static float scaleNumber(float inMin, float inMax, float outMin, float outMax, float value) {
        return (value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
    }

    /**
     * Rounds <code>value</code>. If the difference of the original value and the rounded value is more than <code>maxDiff</code>,
     * the original value will be returned.
     * @param value the number to round.
     * @param maxDiff the maximum difference between the original value and the rounded value.
     * @return the rounded value, or the original value if the difference is more than <code>maxDiff</code>.
     */
    public static float roundWithMaxDifference(float value, float maxDiff) {
        float rounded = Math.round(value);
        if(Math.abs(rounded - value) > maxDiff)
            return value;
        return rounded;
    }

    public static Vector2f getQuadCenter(Vector2f[] vertices) {
        float xSum = 0;
        float ySum = 0;

        for(int i = 0; i < 4; i++) {
            xSum += vertices[i].x;
            ySum += vertices[i].y;
        }

        return new Vector2f(xSum / 4, ySum / 4);
    }

    public static Vector2f[] rotateQuad(Vector2f[] vertices, float angleDegrees) {
        Vector2f center = getQuadCenter(vertices);
        Matrix4f rotationMatrix = new Matrix4f().identity().translate(center.x, center.y, 0f).rotateZ(Math.toRadians(
                angleDegrees)).translate(-center.x, -center.y, 0f);

        Vector2f[] rotatedVertices = new Vector2f[4];
        for(int i = 0; i < 4; i++) {
            Vector3f vertex = new Vector3f(vertices[i], 0f);
            rotationMatrix.transformPosition(vertex);
            rotatedVertices[i] = new Vector2f(vertex.x, vertex.y);
        }

        return rotatedVertices;
    }

}
