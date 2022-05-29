package de.arnomann.martin.blobby.event;

public interface EventListener {

    default void onStart(StartEvent event) {}
    default void onUpdate(UpdateEvent event) {}
    default void onKeyPressed(KeyPressedEvent event) {}
    default void onKeyReleased(KeyReleasedEvent event) {}
    default void onMouseButtonPressed(MouseButtonPressedEvent event) {}

}
