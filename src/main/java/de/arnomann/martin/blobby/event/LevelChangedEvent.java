package de.arnomann.martin.blobby.event;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.levels.Level;

/**
 * An event. Called when the level is changed using {@link BlobbyEngine#setLevel(Level)}
 */
public class LevelChangedEvent extends Event {

    /**
     * The new level.
     */
    public final Level newLevel;

    public LevelChangedEvent(Level newLevel) {
        this.newLevel = newLevel;
    }

}
