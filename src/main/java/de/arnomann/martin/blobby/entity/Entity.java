package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.event.EventListener;
import org.joml.Vector2d;

import java.util.Map;

public class Entity implements EventListener {

    private static int entityCount = 0;
    private long id;

    public Entity(Vector2d position, ITexture texture, Map<String, Object> parameters) {
        entityCount++;
        this.id = entityCount;
    }

    public Vector2d getPosition() {
        return null;
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    public ITexture getTexture() {
        return null;
    }

    public boolean renderInFrontOfPlayer() {
        return true;
    }

    public final long getId() {
        return id;
    }

    public final boolean equals(Object that) {
        if(that == null)
            return false;
        if(!(that instanceof Entity))
            return false;
        return this.getId() == ((Entity) that).getId();
    }
}
