package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.entity.Entity;

import java.util.List;

/**
 * Represents a screen.
 */
public class Screen {

    /**
     * The entities of the screen. Blocks are entities too.
     */
    public final List<Entity> entities;

    /**
     * Creates a new screen.
     * @param entities the entities on this screen. Blocks are entities too.
     */
    public Screen(List<Entity> entities) {
        this.entities = entities;
    }

}
