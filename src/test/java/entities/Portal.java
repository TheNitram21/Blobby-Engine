package entities;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.Entity;
import org.joml.Vector2d;

import java.util.Map;

public class Portal extends Entity {

    private Vector2d position;
    private String text;

    public Portal(Vector2d position, Map<String, Object> parameters) {
        super(position, parameters);
        this.position = position;
        this.text = (String) parameters.get("Text");
    }

    @Override
    public Vector2d getPosition() {
        return position;
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
        return BlobbyEngine.getTexture("Portal");
    }

}
