package de.arnomann.martin.blobby.core.texture;

import org.joml.Vector4f;

public interface ITexture {
    void bind();

    int getWidth();
    int getHeight();
    String getFilename();

    ITexture setFlipped(boolean flipped);
    boolean isFlipped();

    Vector4f colorModifiers = new Vector4f(1f, 1f, 1f, 1f);
    default void setColorModifiers(float red, float green, float blue, float alpha) {
        colorModifiers.x = red;
        colorModifiers.y = green;
        colorModifiers.z = blue;
        colorModifiers.w = alpha;
    }
    default Vector4f getColorModifiers() {
        return colorModifiers;
    }
}
