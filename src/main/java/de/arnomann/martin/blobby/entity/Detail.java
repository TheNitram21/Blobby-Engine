package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2d;

import java.util.Map;

/**
 * A simple detail. Like a different type of block.
 */
public class Detail extends Entity {

    private final Vector2d pos;
    public final ITexture texture;

    public Detail(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.pos = position;
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
