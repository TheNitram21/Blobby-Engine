package de.arnomann.martin.blobby.core.texture;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.event.EventListener;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.UpdateEvent;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a particle. Just an animated texture that will delete itself when getting to the final texture.
 */
public class Particle implements EventListener {

    private static final List<Particle> particles = new ArrayList<>();
    private static final List<Particle> particlesToRemove = new ArrayList<>();

    private final Vector2d position;
    private Vector2f velocity;
    private Vector2f size;
    private final Vector2f sizeDiff;
    private float rotation;
    private final float rotationDiff;
    private final double lifeTime;
    private double age;
    private final ITexture texture;
    private Runnable onDone;

    /**
     * Creates a new particle.
     * @param filename the path to the animated texture.
     * @param position where the particle should be played.
     */
    public Particle(String filename, Vector2d position, Vector2f velocity, Vector2f size, Vector2f finalSize, float rotation,
                    float finalRotation, double lifeTime) {
        this.texture = BlobbyEngine.getTextureWithoutCaching(filename);
        this.position = new Vector2d(position);
        this.velocity = new Vector2f(velocity);
        this.size = new Vector2f(size);
        this.sizeDiff = new Vector2f(finalSize).sub(size);
        this.rotation = rotation;
        this.rotationDiff = finalRotation - rotation;
        this.lifeTime = lifeTime;

        particles.add(this);
        ListenerManager.registerEventListener(this);
    }

    /**
     * Sets what should happen when the particle is removed.
     * @param onDone what should happen.
     */
    public void setOnDone(Runnable onDone) {
        this.onDone = onDone;
    }

    public void bind(int sampler) {
        texture.bind(sampler);
    }

    /**
     * Returns the position where the particle is played.
     * @return the position.
     */
    public Vector2d getPosition() {
        return position;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public float getRotation() {
        return rotation;
    }

    public float getWidth() {
        return size.x;
    }

    public float getHeight() {
        return size.y;
    }

    public String getFilename() {
        return texture.getFilename();
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        position.add(velocity.x * event.deltaTime, velocity.y * event.deltaTime);
        size.add((float) (sizeDiff.x * event.deltaTime / lifeTime), (float) (sizeDiff.y * event.deltaTime / lifeTime));
        rotation += rotationDiff * event.deltaTime / lifeTime;
        age += event.deltaTime;
        if(age >= lifeTime) {
            particlesToRemove.add(this);
            ListenerManager.removeEventListener(this);
        }
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
