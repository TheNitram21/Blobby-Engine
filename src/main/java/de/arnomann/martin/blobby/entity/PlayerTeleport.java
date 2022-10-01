package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.joml.Vector2d;

import java.util.Map;

public class PlayerTeleport extends Entity {

    private final Vector2d position;

    public PlayerTeleport(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);

        this.position = position;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    public void teleportPlayer() {
        BlobbyEngine.getPlayer().setPosition(new Vector2d(getPosition()).add(0, 1));
    }

}
