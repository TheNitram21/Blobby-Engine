package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.entity.Entity;

import java.util.List;

public class Screen {

    public final List<Entity> entities;

    public Screen(List<Entity> entities) {
        this.entities = entities;
    }

}
