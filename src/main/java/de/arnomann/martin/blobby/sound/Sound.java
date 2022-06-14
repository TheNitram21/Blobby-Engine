package de.arnomann.martin.blobby.sound;

public class Sound {

    private String name;
    private final int sourcePointer;
    private final int bufferPointer;

    public Sound(String name, int sourcePointer, int bufferPointer) {
        this.name = name;
        this.sourcePointer = sourcePointer;
        this.bufferPointer = bufferPointer;
    }

    public String getName() {
        return name;
    }

    public int getSourcePointer() {
        return sourcePointer;
    }

    public int getBufferPointer() {
        return bufferPointer;
    }

}
