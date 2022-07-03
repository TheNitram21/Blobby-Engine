package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2d;

import java.util.Map;

/**
 * A basic block entity.
 */
public class Block extends Entity {

    private final Vector2d pos;
    public final ITexture texture;

    public Block(Vector2d pos, Map<String, String> parameters) {
        super(pos, parameters);
        this.pos = pos;
        this.texture = BlobbyEngine.getTexture(parameters.get("Texture"));
    }

    @Override
    public Vector2d getPosition() {
        return pos;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public ITexture getTexture() {
        return texture;
    }
}
