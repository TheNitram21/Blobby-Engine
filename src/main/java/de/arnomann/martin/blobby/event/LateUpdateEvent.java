package de.arnomann.martin.blobby.event;

public class LateUpdateEvent extends UpdateEvent {

    public LateUpdateEvent(double deltaTime, double time) {
        super(deltaTime, time);
    }

}
