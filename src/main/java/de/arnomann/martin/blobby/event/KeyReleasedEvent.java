package de.arnomann.martin.blobby.event;

public class KeyReleasedEvent extends Event {

    public final int key;

    public KeyReleasedEvent(int key) {
        this.key = key;
    }

}
