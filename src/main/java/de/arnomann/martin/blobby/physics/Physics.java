package de.arnomann.martin.blobby.physics;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.joml.Vector2d;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Physics {

    private Physics() {}

    public static double distance(Vector2d a, Vector2d b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public static boolean objectInBox(Vector2d boxStartingPos, double boxWidth, double boxHeight, String entityClassName) {
        AtomicBoolean collides = new AtomicBoolean(false);

        Rectangle.Double r = new Rectangle.Double(boxStartingPos.x, boxStartingPos.y, boxWidth, boxHeight);

        BlobbyEngine.getCurrentLevel().screens.forEach((posS, screen) -> {
            screen.entities.forEach(e -> {
                if(!e.getClass().getSimpleName().equalsIgnoreCase(entityClassName))
                    return;

                if(r.intersects(e.getPosition().x, e.getPosition().y, e.getWidth(), e.getHeight())) {
                    collides.set(true);
                }
            });
        });

        return collides.get();
    }

}
