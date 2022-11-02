package de.arnomann.martin.blobby.entity;

import org.joml.Vector2d;

import java.util.Map;

public class Light extends Entity {

    private final Vector2d position;

    public Light(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.position = position;
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
