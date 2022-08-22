package de.arnomann.martin.blobby.event;

/**
 * A event listener.
 */
public interface EventListener {

    /** Called when the window is opened. */
    default void onStart(StartEvent event) {}
    /** Called each frame. */
    default void onUpdate(UpdateEvent event) {}
    /** Called each frame. Always called after {@link EventListener#onUpdate}. */
    default void onLateUpdate(LateUpdateEvent event) {}
    /** Called when a key is pressed. */
    default void onKeyPressed(KeyPressedEvent event) {}
    /** Called when a key is released. */
    default void onKeyReleased(KeyReleasedEvent event) {}
    /** Called when a mouse button is pressed. */
    default void onMouseButtonPressed(MouseButtonPressedEvent event) {}
    /** Called after rendering blocks and the player, but before UI. */
    default void onMainRenderDone(MainRenderDoneEvent event) {}

}
