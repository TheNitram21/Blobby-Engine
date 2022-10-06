package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import org.joml.Vector2d;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class BaseNPC extends Entity {

    private Vector2d position;

    public BaseNPC(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);

        this.position = position;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector2d position) {
        this.position = position;
    }

    public abstract void moveTo(String x, String y, String unitsPerSecond);
    public abstract void say(String text);
    public abstract void wait(String seconds);
    public abstract void die();

    public final void call(String entityName, String method) {
        Entity target = BlobbyEngine.getCurrentLevel().findEntityByParameter("Name", entityName);
        if(target == null) {
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(), new IllegalStateException(
                    "Can't find entity with 'Name' property of '" + entityName + "'"));
            return;
        }

        try {
            target.getClass().getMethod(method).invoke(target);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
