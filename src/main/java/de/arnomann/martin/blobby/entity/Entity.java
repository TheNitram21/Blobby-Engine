package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.event.EventListener;
import org.joml.Vector2d;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A basic entity.
 */
public abstract class Entity implements EventListener {

    private static int entityCount = 0;

    private long id;
    private final Map<String, String> parameters;

    /**
     * Creates a new entity. SHOULD NOT BE CALLED.
     * @param position the position.
     * @param parameters the parameters.
     */
    public Entity(Vector2d position, Map<String, String> parameters) {
        entityCount++;
        this.id = entityCount;
        this.parameters = parameters;
    }

    /**
     * Returns the position of the entity. OVERRIDE.
     * @return the position.
     */
    public abstract Vector2d getPosition();

    /**
     * Sets the position of the entity. OVERRIDE.
     * @param position the new position.
     */
    public void setPosition(Vector2d position) {}

    /**
     * Returns the width of the entity in units. OVERRIDE.
     * @return the width.
     */
    public abstract int getWidth();

    /**
     * Returns the height of the entity in units. OVERRIDE.
     * @return the height.
     */
    public abstract int getHeight();

    /**
     * Returns the offset of the texture when rendering in units. OVERRIDE.
     * @return the offset.
     */
    public Vector2d getRenderingOffset() {
        return new Vector2d();
    }

    /**
     * Returns the texture of the entity. OVERRIDE.
     * @return the texture.
     */
    public ITexture getTexture() {
        return null;
    }

    /**
     * Returns whether the entity should be rendered in front of the player or not.
     * @return {@code true} if the entity should be rendered in front of the player, {@code false} otherwise.
     */
    public boolean renderInFrontOfPlayer() {
        return true;
    }

    /**
     * Called just after level loading has finished.
     */
    public void initialize() {}

    /**
     * Called just before unloading a level.
     */
    public void destroy() {}

    /**
     * Returns the id of the entity.
     * @return the id.
     */
    public final long getId() {
        return id;
    }

    /**
     * Returns the parameters of this entity.
     * @return the parameters.
     */
    public final Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Checks if two entities are the same.
     * @param that the other object.
     * @return true if {@code that} is not null, an entity and {@code this} and {@code that} have the same id.
     */
    public final boolean equals(Object that) {
        if(that == null)
            return false;
        if(!(that instanceof Entity))
            return false;
        return this.getId() == ((Entity) that).getId();
    }

    public static Entity getEntityById(long id) {
        AtomicReference<Entity> result = new AtomicReference<>(null);

        BlobbyEngine.getCurrentLevel().screens.forEach((screenPos, screen) -> screen.entities.forEach(entity -> {
            if(entity.getId() == id)
                result.set(entity);
        }));

        return result.get();
    }

}
