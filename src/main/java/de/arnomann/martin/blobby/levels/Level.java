package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.entity.Entity;
import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.*;

/**
 * Represents a level.
 */
public class Level {

    /**
     * The title of the level.
     */
    public final String title;
    /**
     * The screens of the level.
     */
    public final Map<Vector2i, Screen> screens;
    /**
     * The background texture.
     */
    public final ITexture backgroundTexture;
    /** The position where the player starts. */
    public final Vector2d playerStartPosition;
    /** The name of the level file (not including <code>maps/</code>, but including <code>.json</code>) */
    public final String fileName;

    private final Vector2i size;
    private final Vector2i firstScreen;
    private Map<String, String> settings;

    /**
     * Creates a new level.
     * @param title the title of the level.
     * @param screens the screens in the level.
     * @param backgroundTexture the texture displayed as the background.
     * @param fileName the name of the level file.
     */
    public Level(String title, Map<Vector2i, Screen> screens, ITexture backgroundTexture, Vector2d playerStartPosition,
                 String fileName) {
        this.title = title;
        this.screens = screens;
        this.backgroundTexture = backgroundTexture;
        this.playerStartPosition = playerStartPosition;
        this.fileName = fileName;
        this.settings = new HashMap<>();

        size = new Vector2i();
        firstScreen = new Vector2i();

        this.screens.forEach((pos, screen) -> {
            firstScreen.x = Math.min(firstScreen.x, pos.x);
            firstScreen.y = Math.min(firstScreen.y, pos.y);
        });

        this.screens.forEach((pos, screen) -> {
            size.x = Math.max(size.x, pos.x - firstScreen.x);
            size.y = Math.max(size.y, pos.y - firstScreen.y);
        });

        size.add(1, 1);
    }

    /**
     * Set a specific setting in this level.
     * @param key the name of the setting.
     * @param value the value.
     */
    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    /**
     * Returns the settings of this level.
     * @return the settings.
     */
    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * Returns a list of all entities.
     * @return all entities.
     */
    public List<Entity> getAllEntities() {
        List<Entity> entities = new ArrayList<>();
        screens.forEach((pos, screen) -> entities.addAll(screen.entities));
        return entities;
    }

    /**
     * Returns the width of the level in screens.
     * @return the width.
     */
    public int getWidthInScreens() {
        return size.x;
    }

    /**
     * Returns the height of the level in screens.
     * @return the height.
     */
    public int getHeightInScreens() {
        return size.y;
    }

    /**
     * Returns x position of the first screen from the bottom left.
     * @return the first screen's x position.
     */
    public int getFirstScreenX() {
        return firstScreen.x;
    }

    /**
     * Returns y position of the first screen from the bottom left.
     * @return the first screen's y position.
     */
    public int getFirstScreenY() {
        return firstScreen.y;
    }

    /**
     * Searches for an entity by parameters.
     * @param parameter the parameter to search for.
     * @param value the value to search for.
     * @return the entity, or null if nothing was found.
     */
    public Entity findEntityByParameter(String parameter, String value) {
        if(parameter.equalsIgnoreCase("Name") && value.equalsIgnoreCase("Player"))
            return BlobbyEngine.getPlayer();

        for(Screen screen : screens.values()) {
            for(Entity entity : screen.entities) {
                if(entity.getParameters().containsKey(parameter)) {
                    if(entity.getParameters().get(parameter).equals(value)) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int hashCode() {
        return title.hashCode() * screens.hashCode() * backgroundTexture.hashCode();
    }
}
