package de.arnomann.martin.blobby.core.texture;

import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * A basic texture.
 */
public interface ITexture {
    /**
     * Binds the texture so that OpenGL knows which texture to render.
     */
    void bind();

    /**
     * Unbinds the texture.
     */
    default void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Returns the width of the texture in pixels.
     * @return the width.
     */
    int getWidth();

    /**
     * Returns the height of the texture in pixels.
     * @return the height.
     */
    int getHeight();

    /**
     * Returns the filename of the texture. <i>null</i> if the texture was read from a buffered image.
     * @return the filename.
     */
    String getFilename();

    /**
     * Sets whether the texture is flipped or not.
     * @param flipped Whether the texture should be flipped or not.
     * @return the texture.
     * @see ITexture#isFlipped()
     */
    ITexture setFlipped(boolean flipped);

    /**
     * Returns whether the texture is flipped
     * @return {@code true} if the texture is flipped, {@code false} otherwise.
     * @see ITexture#setFlipped(boolean)
     */
    boolean isFlipped();

    /**
     * Sets the color modifiers of the texture.
     * @param red the red channel.
     * @param green the green channel.
     * @param blue the blue channel.
     * @param alpha the alpha channel.
     * @see ITexture#getColorModifiers()
     */
    void setColorModifiers(float red, float green, float blue, float alpha);

    /**
     * Returns the color modifiers set with {@link ITexture#setColorModifiers(float, float, float, float)}.
     * @return the color modifiers.
     */
    Vector4f getColorModifiers();

}
