package de.arnomann.martin.blobby.physics;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import org.joml.Vector2d;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A class for basic physics stuff.
 */
public class Physics {

    private Physics() {}

    /**
     * Calculates the distance between two points.
     * @param a point a.
     * @param b point b.
     * @return the distance between point a and point b.
     */
    public static double distance(Vector2d a, Vector2d b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /**
     * Basic collision detection.
     * @param boxStartingPos the starting position of the box.
     * @param boxWidth the width of the box.
     * @param boxHeight the height of the box.
     * @param entityClassName the class of entities to search for.
     * @return {@code true} if an entity with the class {@code entityClassName} is in the box, {@code false} otherwise.
     */
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

    /**
     * Basic collision detection.
     * @param circleCenter the center of the circle.
     * @param circleRadius the radius of the circle.
     * @param entityClassName the class of entities to search for.
     * @return {@code true} if an entity with the class {@code entityClassName} is in the circle, {@code false} otherwise.
     */
    public static boolean objectInCircle(Vector2d circleCenter, double circleRadius, String entityClassName) {
        AtomicBoolean collides = new AtomicBoolean(false);

        BlobbyEngine.getCurrentLevel().screens.forEach((posS, screen) -> {
            screen.entities.forEach(e -> {
                if(!e.getClass().getSimpleName().equalsIgnoreCase(entityClassName))
                    return;

                if(distance(circleCenter, e.getPosition()) < circleRadius) {
                    collides.set(true);
                }
            });
        });

        return collides.get();
    }

}
