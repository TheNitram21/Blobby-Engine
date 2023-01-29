package de.arnomann.martin.blobby.core;

import static org.lwjgl.opengl.GL46.*;

/**
 * An OpenGL framebuffer.
 */
public class Framebuffer {

    private int width, height;
    private int id;
    private int textureId;

    /**
     * Creates a new framebuffer.
     * @param width the framebuffer width in pixels.
     * @param height the framebuffer height in pixels.
     */
    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        create();
    }

    private void create() {
        id = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, id);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
    }

    /**
     * Resizes the framebuffer.
     * @param width the new width.
     * @param height the new height.
     */
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        create();
    }

    /**
     * Returns the framebuffer's id.
     * @return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Binds the framebuffer for usage.
     */
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    /**
     * Unbinds the framebuffer.
     */
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

}
