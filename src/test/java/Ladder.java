import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.Entity;
import org.joml.Vector2d;

import java.util.Map;

public class Ladder extends Entity {

    private Vector2d position;

    public Ladder(Vector2d position, ITexture texture, Map<String, Object> parameters) {
        super(position, texture, parameters);
        this.position = position;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public ITexture getTexture() {
        return BlobbyEngine.getTexture("Ladder");
    }

    @Override
    public boolean renderInFrontOfPlayer() {
        return false;
    }
}
