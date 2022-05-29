package de.arnomann.martin.blobby.event;

public class KeyPressedEvent extends Event {

    public final int key;

    public KeyPressedEvent(int key) {
        this.key = key;
    }

}
