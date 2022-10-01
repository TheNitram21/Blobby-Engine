package de.arnomann.martin.blobby.sound;

import static org.lwjgl.openal.AL11.*;

/**
 * Represents a basic sound. Sound has to be in the <i>OGG Vorbis</i> format.
 */
public class Sound {

    private String name;
    private final int sourcePointer;
    private final int bufferPointer;

    /**
     * Creates a new sound.
     * @param name the name.
     * @param sourcePointer the source pointer.
     * @param bufferPointer the buffer pointer.
     */
    public Sound(String name, int sourcePointer, int bufferPointer) {
        this.name = name;
        this.sourcePointer = sourcePointer;
        this.bufferPointer = bufferPointer;
    }

    /**
     * Returns the name of the sound.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the source pointer of the sound.
     * @return the source pointer.
     */
    public int getSourcePointer() {
        return sourcePointer;
    }

    /**
     * Returns the buffer pointer of the sound.
     * @return the buffer pointer.
     */
    public int getBufferPointer() {
        return bufferPointer;
    }

    /**
     * Sets the volume of this sound.
     * @param volume the new volume.
     */
    public void setVolume(float volume) {
        alSourcef(sourcePointer, AL_GAIN, volume);
    }

}
