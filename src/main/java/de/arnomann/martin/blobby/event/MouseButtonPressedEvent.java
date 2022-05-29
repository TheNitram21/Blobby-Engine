package de.arnomann.martin.blobby.event;

import org.joml.Vector2d;

public class MouseButtonPressedEvent extends KeyPressedEvent {

    public final Vector2d pos;

    public MouseButtonPressedEvent(int key, Vector2d pos) {
        super(key);
        this.pos = pos;
    }

}
