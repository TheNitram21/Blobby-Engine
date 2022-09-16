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
    private final int width;
    private final int height;
    private final boolean inFrontOfPlayer;

    public Detail(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.pos = position;
        this.texture = BlobbyEngine.getTexture(parameters.get("Texture"));
        
        this.width = Integer.parseInt(parameters.get("Width"));
        this.height = Integer.parseInt(parameters.get("Height"));
        this.inFrontOfPlayer = parameters.get("InFrontOfPlayer").equalsIgnoreCase("YES");
    }

    @Override
    public Vector2d getPosition() {
        return pos;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public ITexture getTexture() {
        return texture;
    }

    @Override
    public boolean renderInFrontOfPlayer() {
        return inFrontOfPlayer;
    }
}
