package de.arnomann.martin.blobby;

/**
 * A basic class for configuring the engine.
 */
public class RunConfigurations {

    /** The title of the window. */
    public final String title;
    /** The width of the window. */
    public final int width;
    /** The height of the window. */
    public final int height;
    /** The path to the window icon. */
    public final String iconPath;

    /**
     * Creates a new configuration.
     * @param title the title of the window.
     * @param width the width of the window.
     * @param height the height of the window.
     * @param iconPath the path to the window icon.
     */
    public RunConfigurations(String title, int width, int height, String iconPath) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.iconPath = iconPath;
    }

}
