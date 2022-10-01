package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.sound.SoundPlayer;
import org.joml.Vector2d;

import java.util.Map;

public class Sound extends Entity {

    private final Vector2d position;
    private final String name;
    private final de.arnomann.martin.blobby.sound.Sound sound;
    private final boolean looping;

    public Sound(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.position = position;

        this.name = parameters.get("Name");
        this.sound = SoundPlayer.createSound(parameters.get("Sound"));
        this.looping = parameters.get("Looping").equalsIgnoreCase("YES");

        sound.setVolume(Float.parseFloat(parameters.get("Volume")));
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
        SoundPlayer.playSound(sound, looping);
    }

    public void stopSound() {
        SoundPlayer.stopSound(sound);
    }

}
