package de.arnomann.martin.blobby.core.texture;

import org.joml.Vector2i;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

public class AnimatedTexture implements ITexture {

    private final String filename;
    private final List<Texture> textures;
    private final double animationTimeSecs;

    public AnimatedTexture(double animationTimeSecs, String path) {
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
    }

    @Override
    public void bind() {
        double time = glfwGetTime();
        double timeBetweenTextures = animationTimeSecs / textures.size();
        int texIndex = (int) Math.floor((time % animationTimeSecs) / timeBetweenTextures);

        glBindTexture(GL_TEXTURE_2D, textures.get((int) texIndex).id);
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

}
