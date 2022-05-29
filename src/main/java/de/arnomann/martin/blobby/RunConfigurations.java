package de.arnomann.martin.blobby;

public class RunConfigurations {

    public final String title;
    public final int width;
    public final int height;

    public RunConfigurations(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public static RunConfigurations createDefault(String title, int width, int height) {
        return new RunConfigurations(title, width, height);
    }

}
