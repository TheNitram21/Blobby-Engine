package de.arnomann.martin.blobby.event;

/**
 * An event. Called each frame.
 */
public class UpdateEvent extends Event {

    /**
     * The time between this frame and the previous frame.
     */
    public final double deltaTime;
    /**
     * The time that passed since the {@link StartEvent}.
     */
    public final double time;
    /**
     * The amount of frames rendered per second.
     */
    public final double fps;

    public UpdateEvent(double deltaTime, double time) {
        this.deltaTime = deltaTime;
        this.time = time;
        this.fps = 1 / deltaTime;
    }

}
