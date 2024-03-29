package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Shader;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.event.EventListener;
import de.arnomann.martin.blobby.event.ListenerManager;
import org.joml.Vector2d;

import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A basic entity.
 */
public abstract class Entity implements EventListener {

    private static int entityCount = 0;

    private final long id;
    private final Map<String, String> parameters;
    private boolean disabled;

    /**
     * Creates a new entity. SHOULD NOT BE CALLED.
     * @param position the position.
     * @param parameters the parameters.
     */
    public Entity(Vector2d position, Map<String, String> parameters) {
        entityCount++;
        this.id = entityCount;
        this.parameters = parameters;
        ListenerManager.registerEventListener(this);
    }

    /**
     * Returns the position of the entity.
     * @return the position.
     */
    public abstract Vector2d getPosition();

    /**
     * Sets the position of the entity. The default implementation doesn't do anything.
     * @param position the new position.
     */
    public void setPosition(Vector2d position) {}

    /**
     * Returns the width of the entity in units.
     * @return the width.
     */
    public abstract int getWidth();

    /**
     * Returns the height of the entity in units.
     * @return the height.
     */
    public abstract int getHeight();

    /**
     * Returns the offset of the texture when rendering in units.
     * @return the offset.
     */
    public Vector2d getRenderingOffset() {
        return new Vector2d();
    }

    /**
     * Returns the rotation of the entity in degrees. The rotation will only affect rendering and not the built-in collision checks.
     * @return the rotation.
     */
    public float getRotation() {
        return 0f;
    }

    /**
     * Returns the texture of the entity. If {@code null}, the entity won't be rendered by the default renderer.
     * @return the texture.
     */
    public ITexture getTexture() {
        return null;
    }

    /**
     * Returns the shader which should be used for rendering this entity.
     * @return the shader.
     */
    public Shader getShader() {
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
     * Called just before unloading a level. Should only be called by Blobby Engine internally.
     */
    public final void destroy() {
        disable();
        BlobbyEngine.getCurrentLevel().screens.get(BlobbyEngine.getEntityScreen(this)).entities.remove(this);
    }

    /**
     * Disables this entity. It won't be rendered anymore (if not using custom rendering) and won't receive any events anymore.
     * @see Entity#disabled()
     */
    public void disable() {
        if(disabled)
            return;

        disabled = true;
        ListenerManager.removeEventListener(this);
    }

    /**
     * Returns whether this entity is disabled.
     * @return {@code true}, if this entity is disabled using {@link Entity#disable()}, {@code false} otherwise.
     * @see Entity#disable()
     */
    public boolean disabled() {
        return disabled;
    }

    public void callMethod(String methodName) {
        try {
            getClass().getMethod(methodName).invoke(this);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

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
        return this.getId() == ((Entity) that).getId() && this.disabled() == ((Entity) that).disabled();
    }

    /**
     * Searches for an entity using its id.
     * @param id the id to search for.
     * @return the found entity, if any.
     */
    public static Entity getEntityById(long id) {
        AtomicReference<Entity> result = new AtomicReference<>(null);

        BlobbyEngine.getCurrentLevel().screens.forEach((screenPos, screen) -> screen.entities.forEach(entity -> {
            if(entity.getId() == id)
                result.set(entity);
        }));

        return result.get();
    }

}
