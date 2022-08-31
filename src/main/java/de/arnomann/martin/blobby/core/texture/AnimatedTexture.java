package de.arnomann.martin.blobby.core.texture;

import org.joml.Vector2i;
import org.joml.Vector4f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Represents a texture that has animations.
 */
public class AnimatedTexture implements ITexture {

    private final String filename;
    protected final List<Texture> textures;
    private final double animationTimeSecs;

    private double startingTime;
    private boolean playAnimation = false;
    private boolean flipped = false;

    private int textureIndex;

    /**
     * Creates a new animated texture.
     * @param animationTimeSecs the time the animation will take.
     * @param path the path to the textures.
     */
    public AnimatedTexture(double animationTimeSecs, String path) {
        this(animationTimeSecs, path, true, 0);
    }

    /**
     * Creates a new animated texture.
     * @param animationTimeSecs the time the animation will take.
     * @param path the path to the textures.
     * @param playOnStart whether the animation should start right away or not.
     */
    public AnimatedTexture(double animationTimeSecs, String path, boolean playOnStart) {
        this(animationTimeSecs, path, playOnStart, 0);
    }

    /**
     * Creates a new animated texture.
     * @param animationTimeSecs the time the animation will take.
     * @param path the path to the textures.
     * @param startingFrame the frame id on which the animation should start.
     */
    public AnimatedTexture(double animationTimeSecs, String path, int startingFrame) {
        this(animationTimeSecs, path, true, startingFrame);
    }

    /**
     * Creates a new animated texture.
     * @param animationTimeSecs the time the animation will take.
     * @param path the path to the textures.
     * @param playOnStart whether the animation should start right away or not.
     * @param startingFrame the frame id on which the animation should start.
     */
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

        startingTime -= (animationTimeSecs / this.textures.size()) * startingFrame;
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

    Vector4f colorModifiers = new Vector4f(1, 1, 1, 1);

    @Override
    public void setColorModifiers(float red, float green, float blue, float alpha) {
        colorModifiers.x = red;
        colorModifiers.y = green;
        colorModifiers.z = blue;
        colorModifiers.w = alpha;
    }

    @Override
    public Vector4f getColorModifiers() {
        return colorModifiers;
    }

    /**
     * Starts the animation on the first frame.
     * @see AnimatedTexture#startAnimation(int)
     * @see AnimatedTexture#stopAnimation()
     */
    public void startAnimation() {
        startingTime = glfwGetTime();
        playAnimation = true;
    }

    /**
     * Starts the animation on a specified frame.
     * @param frame the frame on which the animation should start.
     * @see AnimatedTexture#startAnimation()
     * @see AnimatedTexture#stopAnimation()
     */
    public void startAnimation(int frame) {
        startAnimation();

        startingTime -= (animationTimeSecs / this.textures.size()) * frame;
    }

    /**
     * Stops the animation.
     * @see AnimatedTexture#startAnimation()
     * @see AnimatedTexture#startAnimation(int)
     */
    public void stopAnimation() {
        playAnimation = false;
    }

    /**
     * Returns the current texture index. This will not be calculated in this method.
     * @return the texture index.
     * @see AnimatedTexture#calculateTextureIndex()
     */
    public int getTextureIndex() {
        return textureIndex;
    }

    /**
     * Returns the amount of frames in the animation.
     * @return the frame count.
     */
    public int getFrameCount() {
        return textures.size();
    }

    /**
     * Calculates the texture index. Will be stored in a private variable.
     * @see AnimatedTexture#getTextureIndex()
     */
    private void calculateTextureIndex() {
        double time = glfwGetTime() - startingTime;
        double timeBetweenTextures = animationTimeSecs / textures.size();
        textureIndex = (int) Math.floor((time % animationTimeSecs) / timeBetweenTextures);
    }

}
