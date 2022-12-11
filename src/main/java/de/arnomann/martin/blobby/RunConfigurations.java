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
    /** Whether the window should be in fullscreen or not. */
    public final boolean fullscreen;
    public final int multiSampling;

    /**
     * Creates a new configuration.
     * @param title the title of the window.
     * @param width the width of the window.
     * @param height the height of the window.
     * @param iconPath the path to the window icon.
     * @param fullscreen {@code true} if the game is started in fullscreen mode, {@code false} otherwise.
     * @param multiSampling the amount of samples in a single pixel. Used for anti-aliasing. Usually a power of 2.
     */
    public RunConfigurations(String title, int width, int height, String iconPath, boolean fullscreen, int multiSampling) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.iconPath = iconPath;
        this.fullscreen = fullscreen;
        this.multiSampling = multiSampling;
    }

    /**
     * Creates a new configuration with the default settings.
     * @param title the title of the window.
     * @param width the width of the window.
     * @param height the height of the window.
     * @param iconPath the path to the window icon.
     * @param fullscreen {@code true} if the game is started in fullscreen mode, {@code false} otherwise.
     */
    public static RunConfigurations createDefault(String title, int width, int height, String iconPath, boolean fullscreen) {
        return new RunConfigurations(title, width, height, iconPath, fullscreen, 0);
    }

}
