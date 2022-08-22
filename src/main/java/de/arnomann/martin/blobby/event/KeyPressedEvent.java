package de.arnomann.martin.blobby.event;

/**
 * An event. Called when a key is pressed.
 */
public class KeyPressedEvent extends Event {

    /**
     * The key code.
     */
    public final int key;

    public KeyPressedEvent(int key) {
        this.key = key;
    }

}
