package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2i;

import java.util.Map;

public class Level {

    public final String title;
    public final Map<Vector2i, Screen> screens;
    public final ITexture backgroundTexture;

    public Level(String title, Map<Vector2i, Screen> screens, ITexture backgroundTexture) {
        this.title = title;
        this.screens = screens;
        this.backgroundTexture = backgroundTexture;
    }

}
