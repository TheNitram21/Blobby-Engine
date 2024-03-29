package de.arnomann.martin.blobby.physics;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.entity.Player;
import org.joml.Math;
import org.joml.Vector2d;

import java.awt.*;
import java.util.Arrays;
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
        double dx = a.x - b.x;
        double dy = a.y - b.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Basic collision detection.
     * @param boxStartingPos the starting position of the box.
     * @param boxWidth the width of the box.
     * @param boxHeight the height of the box.
     * @param entityClassNames the classes of entities to search for.
     * @return {@code true} if an entity with any of the classes {@code entityClassNames} is in the box, {@code false} otherwise.
     */
    public static boolean objectInBox(Vector2d boxStartingPos, double boxWidth, double boxHeight, String... entityClassNames) {
        AtomicBoolean collides = new AtomicBoolean(false);

        Rectangle.Double r = new Rectangle.Double(boxStartingPos.x, boxStartingPos.y, boxWidth, boxHeight);

        if(Arrays.asList(entityClassNames).contains("Player")) {
            Player p = BlobbyEngine.getPlayer();
            collides.set(r.intersects(p.getPosition().x, p.getPosition().y, p.getWidth(), p.getHeight()));
        }

        BlobbyEngine.getCurrentLevel().screens.forEach((posS, screen) -> screen.entities.forEach(e -> {
            if(e.disabled())
                return;
            if(!Arrays.asList(entityClassNames).contains(e.getClass().getSimpleName()))
                return;

            if(r.intersects(e.getPosition().x, e.getPosition().y, e.getWidth(), e.getHeight())) {
                collides.set(true);
            }
        }));

        return collides.get();
    }

    /**
     * Basic collision detection.
     * @param circleCenter the center of the circle.
     * @param circleRadius the radius of the circle.
     * @param entityClassNames the classes of entities to search for.
     * @return {@code true} if an entity with any of the classes {@code entityClassNames} is in the circle, {@code false} otherwise.
     */
    public static boolean objectInCircle(Vector2d circleCenter, double circleRadius, String... entityClassNames) {
        AtomicBoolean collides = new AtomicBoolean(false);

        if(Arrays.asList(entityClassNames).contains("Player")) {
            Player p = BlobbyEngine.getPlayer();
            collides.set(distance(circleCenter, p.getPosition()) < circleRadius);
        }

        BlobbyEngine.getCurrentLevel().screens.forEach((posS, screen) -> screen.entities.forEach(e -> {
            if(e.disabled())
                return;
            if(!Arrays.asList(entityClassNames).contains(e.getClass().getSimpleName()))
                return;

            if(distance(circleCenter, e.getPosition()) < circleRadius) {
                collides.set(true);
            }
        }));

        return collides.get();
    }

    public static double raycast(Vector2d rayStartingPos, Vector2d rayEndPos, String entityClassName) {
        double distance = -1;
        
        Vector2d step = getDirection(rayStartingPos, rayEndPos).mul(0.1);
        Vector2d pos = new Vector2d(rayStartingPos);

        double rayLength = distance(rayStartingPos, rayEndPos);
        while(distance(rayStartingPos, pos) < rayLength) {
            pos.add(step);

            if(objectInCircle(pos, 0.05, entityClassName)) {
                distance = distance(rayStartingPos, pos);
            }
        }

        if(distance > rayLength)
            distance = rayLength;

        return distance;
    }

    /**
     * Returns the position from one position to another one.
     * @param from the beginning position.
     * @param to the end position.
     * @return the direction.
     */
    public static Vector2d getDirection(Vector2d from, Vector2d to) {
        return new Vector2d(to).sub(from).normalize();
    }

}
