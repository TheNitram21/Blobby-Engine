package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.event.UpdateEvent;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.physics.Physics;
import org.joml.Vector2d;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Trigger extends Entity {

    private final Vector2d position;
    private final Entity target;
    private final String method;
    private final boolean onlyOnce;

    private boolean triggered;
    private boolean triggeredLastFrame;

    public Trigger(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);

        this.position = position;
        this.target = BlobbyEngine.getCurrentLevel().findEntityByParameter("Name", parameters.get("Target"));
        if(this.target == null)
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(), new IllegalStateException(
                    "Can't find entity with 'Name' property of '" + parameters.get("Target") + "'"));

        this.method = parameters.get("Method");
        this.onlyOnce = Boolean.parseBoolean(parameters.get("OnlyOnce"));

        this.triggered = false;
        this.triggeredLastFrame = false;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if(!onlyOnce || !triggered) {
            if(Physics.objectInBox(new Vector2d(getPosition()).add(0, 1), getWidth(), getHeight(),
                    "Player")) {
                if(!triggeredLastFrame) {
                    try {
                        target.getClass().getMethod(method).invoke(target);
                    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }

                triggered = true;
                triggeredLastFrame = true;
            } else {
                if(!triggered)
                    triggeredLastFrame = false;
                triggered = false;
            }
        }
    }

}
