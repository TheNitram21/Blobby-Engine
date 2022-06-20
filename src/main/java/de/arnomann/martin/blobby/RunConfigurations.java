package de.arnomann.martin.blobby;

public class RunConfigurations {

    public final String title;
    public final int width;
    public final int height;
    public final String iconPath;

    public RunConfigurations(String title, int width, int height, String iconPath) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.iconPath = iconPath;
    }

    public static RunConfigurations createDefault(String title, int width, int height, String iconPath) {
        return new RunConfigurations(title, width, height, iconPath);
    }

}
