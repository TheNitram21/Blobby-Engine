package de.arnomann.martin.blobby.levels;

import org.joml.Vector2i;

import java.util.Map;

public class Level {

    public final String title;
    public final Map<Vector2i, Screen> screens;

    public Level(String title, Map<Vector2i, Screen> screens) {
        this.title = title;
        this.screens = screens;
    }

}
