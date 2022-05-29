package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.levels.Screen;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public final class Renderer {

    private static Map<Vector4i, ITexture> queuedTextures = new HashMap<>();
    private static Map<Vector4f, ITexture> queuedUITextures = new HashMap<>();

    private static Window curWindow;

    private Renderer() {}

    /* ONLY FOR INTERNAL USE */
    public static void queueUITexture(Vector2f uvStart, Vector2f uvEnd, ITexture texture) {
        uvStart.x = uvStart.x * 2 - 1;
        uvStart.y = -(uvStart.y * 2 - 1);
        uvEnd.x = uvEnd.x * 2 - 1;
        uvEnd.y = -(uvEnd.y * 2 - 1);

        queuedUITextures.put(new Vector4f(uvStart.x, uvStart.y, uvEnd.x, uvEnd.y), texture);
    }

    public static void queueTexture(int x, int y, int width, int height, ITexture texture) {
        queuedTextures.put(new Vector4i(x, y, width, height), texture);
    }

    public static void render(Window window) {
        curWindow = window;
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        int textureWidth = (int) BlobbyEngine.unitMultiplier();
        int textureHeight = (int) BlobbyEngine.unitMultiplier();

        Level level = BlobbyEngine.getCurrentLevel();

        Player player = BlobbyEngine.getPlayer();
        System.out.println(player.getPosition().x + " " + player.getPosition().y);

        if(level != null) {
            Vector2i playerScreenPos = BlobbyEngine.getEntityScreen(player);

            level.screens.forEach((screenPos, screen) -> {
                if(screenPos.equals(playerScreenPos)) {
                    screen.entities.forEach(entity -> {
                        Vector2d entityPos = new Vector2d(entity.getPosition()).mul(BlobbyEngine.unitMultiplier());
                        queueTexture((int) (entityPos.x), (int) (entityPos.y), textureWidth, textureHeight, entity.getTexture());
                    });
                }
            });
        }

        if(BlobbyEngine.renderPlayer) {
            render((int) (textureWidth * (player.getPosition().x % 16)), (int) (textureHeight * (player.getPosition().y % 9) - textureHeight * 2),
                    textureWidth, textureHeight * 2, player.getTexture());
        }

        queuedTextures.forEach((pos, tex) -> {
            render(pos.x, pos.y, pos.z, pos.w, tex);
        });

        queuedUITextures.forEach((uvs, tex) -> {
            renderUV(new Vector2f(uvs.x, uvs.y), new Vector2f(uvs.z, uvs.w), tex);
        });

        if(BlobbyEngine.showMenu && BlobbyEngine.menu != null)
            renderMenu(BlobbyEngine.menu);

        glfwSwapBuffers(window.getId());
        queuedTextures.clear();
    }

    public static void renderUV(Vector2f uvStart, Vector2f uvEnd, ITexture texture) {
        texture.bind();

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(uvStart.x, uvStart.y);
        glTexCoord2f(1, 0);
        glVertex2f(uvEnd.x, uvStart.y);
        glTexCoord2f(1, 1);
        glVertex2f(uvEnd.x, uvEnd.y);
        glTexCoord2f(0, 1);
        glVertex2f(uvStart.x, uvEnd.y);
        glEnd();
    }

    public static void renderMenu(Menu menu) {
        renderUV(new Vector2f(-1f, -1f), new Vector2f(-0.5f, 1f), menu.getBackgroundTexture());
        for(Button b : menu.getButtons()) {
            renderUV(b.uvStart, b.uvEnd, b.texture);
        }
    }

    public static void render(int x, int y, int width, int height, ITexture texture) {
        texture.bind();

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(windowXToVertexX(x), windowYToVertexY(y));
        glTexCoord2f(1, 0);
        glVertex2f(windowXToVertexX(x + width), windowYToVertexY(y));
        glTexCoord2f(1, 1);
        glVertex2f(windowXToVertexX(x + width), windowYToVertexY(y + height));
        glTexCoord2f(0, 1);
        glVertex2f(windowXToVertexX(x), windowYToVertexY(y + height));
        glEnd();
    }

    public static float windowXToVertexX(int x) {
        return (float) (2.0 * x / curWindow.getWidth() - 1);
    }

    public static float windowYToVertexY(int y) {
        return (float) (1.0 - 2.0 * y / curWindow.getHeight());
    }

}
