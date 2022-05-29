package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2d;

import java.util.Map;

public class Block extends Entity {

    private final Vector2d pos;
    public final ITexture texture;

    public Block(Vector2d pos, ITexture texture, Map<String, Object> parameters) {
        super(pos, texture, parameters);
        this.pos = pos;
        this.texture = texture;
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
