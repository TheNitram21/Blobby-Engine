package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2i;

import java.util.Map;

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

    public Level(String title, Map<Vector2i, Screen> screens, ITexture backgroundTexture) {
        this.title = title;
        this.screens = screens;
        this.backgroundTexture = backgroundTexture;
    }

}
