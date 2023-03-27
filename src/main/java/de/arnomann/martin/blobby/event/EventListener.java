package de.arnomann.martin.blobby.event;

import de.arnomann.martin.blobby.SaveManager;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.levels.Level;

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
    /** Called when the window is closed. */
    default void onStop(StopEvent event) {}
    /** Called when a key is pressed. */
    default void onKeyPressed(KeyPressedEvent event) {}
    /** Called when a key is released. */
    default void onKeyReleased(KeyReleasedEvent event) {}
    /** Called when a mouse button is pressed. */
    default void onMouseButtonPressed(MouseButtonPressedEvent event) {}
    /** Called after finishing rendering a render step. */
    default void onRenderStepDone(RenderStepDoneEvent event) {}
    /**
     * Called after the level is changed using {@link BlobbyEngine#setLevel(Level)}
     */
    default void onLevelChanged(LevelChangedEvent event) {}
    /** Called after a save file is saved using {@link SaveManager#save()} */
    default void onSave(SaveEvent event) {}
    /** Called after a save file is loaded using {@link SaveManager#load()} */
    default void onLoad(LoadEvent event) {}

}
