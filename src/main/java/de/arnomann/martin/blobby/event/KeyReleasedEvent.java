package de.arnomann.martin.blobby.event;

/**
 * An event. Called when a key is pressed.
 */
public class KeyReleasedEvent extends Event {

    /**
     * The key code.
     */
    public final int key;

    public KeyReleasedEvent(int key) {
        this.key = key;
    }

}
