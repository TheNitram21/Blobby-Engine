package de.arnomann.martin.blobby.ui;

import de.arnomann.martin.blobby.core.Renderer;
import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2f;

/**
 * A utility class for managing UI.
 */
public class UI {

    private UI() {}

    /**
     * Draws a UI texture on the screen.
     * @param uvStart the starting position of the UI in UV space.
     * @param uvEnd the end position of the UI in UV space.
     * @param texture the texture of the UI.
     */
    public static void drawUI(Vector2f uvStart, Vector2f uvEnd, ITexture texture) {
        Renderer.queueUITexture(uvStart, uvEnd, texture);
    }

}
