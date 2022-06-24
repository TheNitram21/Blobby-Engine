package de.arnomann.martin.blobby.core.texture;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class Particle implements ITexture {

    private static final List<Particle> particles = new ArrayList<>();
    private static final List<Particle> particlesToRemove = new ArrayList<>();

    private final Vector2d position;
    private final AnimatedTexture animTex;

    public Particle(String filename, Vector2d position) {
        this.animTex = (AnimatedTexture) BlobbyEngine.getTextureWithoutCaching(filename);
        this.position = position;
        particles.add(this);
    }

    @Override
    public void bind() {
        animTex.bind();

        if(animTex.getTextureIndex() == animTex.getFrameCount() - 1) {
            particlesToRemove.add(this);
        }
    }

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

    public static List<Particle> getParticles() {
        return particles;
    }

    public static void updateParticleList() {
        particles.removeAll(particlesToRemove);
    }

}
