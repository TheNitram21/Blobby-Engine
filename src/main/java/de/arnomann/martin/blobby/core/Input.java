package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.event.KeyPressedEvent;
import de.arnomann.martin.blobby.event.KeyReleasedEvent;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.MouseButtonPressedEvent;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    private static List<Integer> keys = new ArrayList<>();
    private static boolean initialized = false;

    public static void initialize() {
        initialized = true;

        glfwSetKeyCallback(BlobbyEngine.getWindow().getId(), (windowId, key, scancode, action, mods) -> {
            if(action == GLFW_PRESS) {
                ListenerManager.callEvent(new KeyPressedEvent(key));
                keys.add(key);
            } else if(action == GLFW_RELEASE) {
                ListenerManager.callEvent(new KeyReleasedEvent(key));
                keys.remove(Integer.valueOf(key));
            }
        });

        glfwSetMouseButtonCallback(BlobbyEngine.getWindow().getId(), (windowId, key, action, mods) -> {
            if(action == GLFW_PRESS) {
                DoubleBuffer cursorX = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer cursorY = BufferUtils.createDoubleBuffer(1);

                glfwGetCursorPos(windowId, cursorX, cursorY);
                ListenerManager.callEvent(new MouseButtonPressedEvent(key, new Vector2d(cursorX.get(), cursorY.get())));
                keys.add(key);
            } else if(action == GLFW_RELEASE) {
                keys.remove(Integer.valueOf(key));
            }
        });

        BlobbyEngine.getLogger().info("Initialized input");
    }

    public static boolean keyPressed(int keyCode) {
        return keys.contains(keyCode);
    }

}
