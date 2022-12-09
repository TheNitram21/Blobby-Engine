package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.event.KeyPressedEvent;
import de.arnomann.martin.blobby.event.UpdateEvent;
import de.arnomann.martin.blobby.physics.Physics;
import org.joml.Vector2d;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class Button extends Trigger {

    /** The key the button will react to. */
    public static int keyCode = GLFW_KEY_E;

    private ITexture texture;
    private boolean couldTrigger = false;

    public Button(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.texture = BlobbyEngine.getTexture(parameters.get("Texture"));
    }

    @Override
    public ITexture getTexture() {
        return texture;
    }

    // This override is just to prevent the button being "pressed" when a player walks into it, as this class extends
    // the Trigger class.
    @Override
    public void onUpdate(UpdateEvent event) {
        if(Physics.objectInBox(new Vector2d(getPosition()).add(0, 2), getWidth(), getHeight(),
                "Player")) {
            if(!triggeredLastFrame) {
                couldTrigger = true;
            }

            triggered = true;
            triggeredLastFrame = true;
        } else {
            couldTrigger = false;

            if(!triggered)
                triggeredLastFrame = false;
            triggered = false;
        }
    }

    @Override
    public void onKeyPressed(KeyPressedEvent event) {
        if(event.key != keyCode) return;

        if(couldTrigger) {
            try {
                target.getClass().getMethod(method).invoke(target);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

}
