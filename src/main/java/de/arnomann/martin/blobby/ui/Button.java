package de.arnomann.martin.blobby.ui;

import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.event.EventListener;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.MouseButtonPressedEvent;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class Button implements EventListener {

    public final Vector2f uvStart;
    public final Vector2f uvEnd;
    public final ITexture texture;
    private final Runnable onClick;

    public Button(Vector2f uvStart, Vector2f uvEnd, ITexture texture, Runnable onClick) {
        this.uvStart = new Vector2f(uvStart.x * 2 - 1, -(uvStart.y * 2 - 1));
        this.uvEnd = new Vector2f(uvEnd.x * 2 - 1, -(uvEnd.y * 2 - 1));
        this.texture = texture;
        this.onClick = onClick;
        ListenerManager.registerEventListener(this);
    }

    @Override
    public void onMouseButtonPressed(MouseButtonPressedEvent event) {
        double clickXPos = (event.pos.x / BlobbyEngine.getWindow().getWidth()) * 2 - 1;
        double clickYPos = -((event.pos.y / BlobbyEngine.getWindow().getHeight()) * 2 - 1);

        if(event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && (MathUtil.inclusiveBetween(uvStart.x, uvEnd.x, clickXPos) &&
                MathUtil.inclusiveBetween(uvEnd.y, uvStart.y, clickYPos))) {
            onClick.run();
        }
    }
}
