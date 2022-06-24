package de.arnomann.martin.blobby.core.texture;

import org.joml.Vector2i;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

public class AnimatedTexture implements ITexture {

    private final String filename;
    private final List<Texture> textures;
    private final double animationTimeSecs;

    private double startingTime;
    private boolean playAnimation = false;
    private boolean flipped = false;

    private int textureIndex;

    public AnimatedTexture(double animationTimeSecs, String path) {
        this(animationTimeSecs, path, true, 0);
    }

    public AnimatedTexture(double animationTimeSecs, String path, boolean playOnStart) {
        this(animationTimeSecs, path, playOnStart, 0);
    }

    public AnimatedTexture(double animationTimeSecs, String path, int startingFrame) {
        this(animationTimeSecs, path, true, startingFrame);
    }

    public AnimatedTexture(double animationTimeSecs, String path, boolean playOnStart, int startingFrame) {
        filename = path;
        Texture[] textures = new Texture[new File(path).list().length - 1];
        for(int i = 0; i < textures.length; i++) {
            textures[i] = new Texture(path + "/" + i);
        }

        this.textures = new ArrayList<>(textures.length);

        Vector2i prevSize = new Vector2i(textures[0].getWidth(), textures[0].getHeight());
        for(Texture t : textures) {
            if(new Vector2i(t.getWidth(), t.getHeight()).equals(prevSize)) {
                prevSize = new Vector2i(t.getWidth(), t.getHeight());
                this.textures.add(t);
            }
        }

        this.animationTimeSecs = animationTimeSecs;

        if(playOnStart)
            startAnimation();

        startingTime = (animationTimeSecs / this.textures.size()) * startingFrame;
    }

    @Override
    public void bind() {
        if(playAnimation) {
            calculateTextureIndex();

            glBindTexture(GL_TEXTURE_2D, textures.get(textureIndex).id);
        } else {
            glBindTexture(GL_TEXTURE_2D, textures.get(0).id);
        }
    }

    @Override
    public int getWidth() {
        return textures.get(0).getWidth();
    }

    @Override
    public int getHeight() {
        return textures.get(0).getHeight();
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public ITexture setFlipped(boolean flipped) {
        this.flipped = flipped;
        return this;
    }

    @Override
    public boolean isFlipped() {
        return flipped;
    }

    public void startAnimation() {
        startingTime = glfwGetTime();
        playAnimation = true;
    }

    public void stopAnimation() {
        playAnimation = false;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    private void calculateTextureIndex() {
        double time = glfwGetTime() - startingTime;
        double timeBetweenTextures = animationTimeSecs / textures.size();
        textureIndex = (int) Math.floor((time % animationTimeSecs) / timeBetweenTextures);
    }

}
