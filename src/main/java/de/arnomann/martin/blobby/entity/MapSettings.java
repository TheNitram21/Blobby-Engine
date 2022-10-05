package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.joml.Vector2d;

import java.util.Map;

public class MapSettings extends Entity {

    private final Vector2d position;

    public MapSettings(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);

        this.position = position;
    }

    @Override
    public void initialize() {
        String key = getParameters().get("Key");
        String value = getParameters().get("Value");

        if(!key.isBlank() && !value.isBlank())
            BlobbyEngine.getCurrentLevel().setSetting(getParameters().get("Key"), getParameters().get("Value"));
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

}
