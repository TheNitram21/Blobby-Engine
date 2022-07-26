package de.arnomann.martin.blobby.event;

import org.joml.Vector2d;

/**
 * An event. Called when a mouse button is pressed.
 */
public class MouseButtonPressedEvent extends KeyPressedEvent {

    /**
     * The position where the mouse is pressed down.
     */
    public final Vector2d pos;

    public MouseButtonPressedEvent(int key, Vector2d pos) {
        super(key);
        this.pos = pos;
    }

}
