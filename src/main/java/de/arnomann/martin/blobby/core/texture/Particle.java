package de.arnomann.martin.blobby.core.texture;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.joml.Vector2d;
import org.joml.Vector4f;

import javax.security.auth.callback.Callback;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a particle. Just an animated texture that will delete itself when getting to the final texture.
 */
public class Particle implements ITexture {

    private static final List<Particle> particles = new ArrayList<>();
    private static final List<Particle> particlesToRemove = new ArrayList<>();

    private final Vector2d position;
    private final AnimatedTexture animTex;
    private Runnable onDone;

    /**
     * Creates a new particle.
     * @param filename the path to the animated texture.
     * @param position where the particle should be played.
     */
    public Particle(String filename, Vector2d position) {
        this.animTex = (AnimatedTexture) BlobbyEngine.getTextureWithoutCaching(filename);
        animTex.textures.add(new Texture(new BufferedImage(animTex.getWidth(), animTex.getHeight(),
                BufferedImage.TYPE_INT_ARGB)));
        this.position = position;
        particles.add(this);
    }

    /**
     * Sets what should happen when the particle is removed.
     * @param onDone what should happen.
     */
    public void setOnDone(Runnable onDone) {
        this.onDone = onDone;
    }

    @Override
    public void bind() {
        animTex.bind();

        if(animTex.getTextureIndex() == animTex.getFrameCount() - 1) {
            particlesToRemove.add(this);
            onDone.run();
        }
    }

    /**
     * Returns the position where the particle is played.
     * @return the position.
     */
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return animTex.getWidth();
    }

    @Override
    public int getHeight() {
        return animTex.getHeight();
    }

    @Override
    public String getFilename() {
        return animTex.getFilename();
    }

    @Override
    public ITexture setFlipped(boolean flipped) {
        animTex.setFlipped(flipped);
        return animTex;
    }

    @Override
    public boolean isFlipped() {
        return animTex.isFlipped();
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
     * Returns a list of all particles.
     * @return all particles.
     */
    public static List<Particle> getParticles() {
        return particles;
    }

    /**
     * Updates the list of paricles.
     */
    public static void updateParticleList() {
        particles.removeAll(particlesToRemove);
    }

}
