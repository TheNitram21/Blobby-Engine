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
    private final int width;
    private final int height;

    protected Entity target;
    protected final String method;
    protected final boolean onlyOnce;

    protected boolean triggered;
    protected boolean triggeredLastFrame;

    public Trigger(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);

        this.position = position;
        this.width = Integer.parseInt(parameters.get("Width"));
        this.height = Integer.parseInt(parameters.get("Height"));

        this.method = parameters.get("Method");
        this.onlyOnce = parameters.get("OnlyOnce").equalsIgnoreCase("YES");

        this.triggered = false;
        this.triggeredLastFrame = false;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void initialize() {
        this.target = BlobbyEngine.getCurrentLevel().findEntityByParameter("Name", getParameters().get("Target"));
        if(this.target == null)
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(), new IllegalStateException(
                    "Can't find entity with 'Name' property of '" + getParameters().get("Target") + "'"));
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if(!onlyOnce || !triggered) {
            if(Physics.objectInBox(new Vector2d(getPosition()).add(0, 2), getWidth(), getHeight(),
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
