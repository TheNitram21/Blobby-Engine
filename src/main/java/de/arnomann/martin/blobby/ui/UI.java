package de.arnomann.martin.blobby.ui;

import de.arnomann.martin.blobby.core.Renderer;
import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2f;

public class UI {

    private UI() {}

    public static void drawUI(Vector2f uvStart, Vector2f uvEnd, ITexture texture) {
        Renderer.queueUITexture(uvStart, uvEnd, texture);
    }

}
