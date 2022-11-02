package de.arnomann.martin.blobby.core;

import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {

    private int width, height;
    private int id;
    private int textureId;

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

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        create();
    }

    public int getId() {
        return id;
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

}
