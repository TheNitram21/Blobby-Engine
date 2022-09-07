package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Renderer;
import de.arnomann.martin.blobby.levels.LevelLoader;
import org.joml.Vector2d;

import java.util.Map;

public class LevelChange extends Entity {

    private Vector2d position;
    private String nextLevel;

    public LevelChange(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.position = position;
        this.nextLevel = parameters.get("NextLevel");
    }

    @Override
    public Vector2d getPosition() {
        return null;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public void changeLevel() {
        LevelLoader.loadLevel(nextLevel, BlobbyEngine::setLevel);
    }

}
