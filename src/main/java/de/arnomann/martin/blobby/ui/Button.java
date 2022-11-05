package de.arnomann.martin.blobby.ui;

import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.event.EventListener;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.MouseButtonPressedEvent;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

/**
 * Represents a UI button.
 */
public class Button implements EventListener {

    /** The starting position of the button in UV space. */
    public final Vector2f uvStart;
    /** The end position of the button in UV space. */
    public final Vector2f uvEnd;
    /** The texture of the button. */
    public final ITexture texture;
    private final Runnable onClick;
    private boolean visible;

    /**
     * Creates a new button.
     * @param uvStart the starting position of the button in UV space.
     * @param uvEnd the end position of the button in UV space.
     * @param texture the texture of the button.
     * @param onClick what happens when the button is clicked.
     */
    public Button(Vector2f uvStart, Vector2f uvEnd, ITexture texture, Runnable onClick) {
        this.uvStart = new Vector2f(uvStart.x * 2 - 1, -(uvStart.y * 2 - 1));
        this.uvEnd = new Vector2f(uvEnd.x * 2 - 1, -(uvEnd.y * 2 - 1));
        this.texture = texture;
        this.onClick = onClick;
        this.visible = false;
        ListenerManager.registerEventListener(this);
    }

    @Override
    public void onMouseButtonPressed(MouseButtonPressedEvent event) {
        double clickXPos = (event.pos.x / BlobbyEngine.getWindow().getWidth()) * 2 - 1;
        double clickYPos = -((event.pos.y / BlobbyEngine.getWindow().getHeight()) * 2 - 1);

        if(visible && event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && (MathUtil.inclusiveBetween(uvStart.x, uvEnd.x,
                clickXPos) && MathUtil.inclusiveBetween(uvEnd.y, uvStart.y, clickYPos))) {
            onClick.run();
        }
    }

    /**
     * Returns the width of the button.
     * @return the width.
     */
    public float getWidth() {
        return uvEnd.x - uvStart.x;
    }

    /**
     * Returns the height of the button.
     * @return the height.
     */
    public float getHeight() {
        return uvEnd.y - uvStart.y;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public boolean isVisible() {
        return visible;
    }

}
