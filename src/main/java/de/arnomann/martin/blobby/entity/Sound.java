package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.sound.SoundPlayer;
import org.joml.Vector2d;

import java.util.Map;

public class Sound extends Entity {

    private final Vector2d position;
    private final String name;
    private final String sound;

    public Sound(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.position = position;

        this.name = parameters.get("Name");
        this.sound = parameters.get("Sound");
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public void playSound() {
        SoundPlayer.playSound(sound);
    }

}
