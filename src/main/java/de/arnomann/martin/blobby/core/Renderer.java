package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static de.arnomann.martin.blobby.MathUtil.booleanToInt;

public final class Renderer {

    private static Map<Vector4i, ITexture> queuedTextures = new HashMap<>();
    private static Map<Vector4f, ITexture> queuedUITextures = new HashMap<>();

    private static Window curWindow;

    private static Vector2d entityOffset = new Vector2d();
    private static Vector2i currentScreen;
    private static double screenTransition = 0d;

    private static double screenTransitionDuration = 1d; // seconds

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

    public static void render(Window window, float deltaTime) {
        curWindow = window;
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Level level = BlobbyEngine.getCurrentLevel();

        Player player = BlobbyEngine.getPlayer();

        Vector2i playerScreen = BlobbyEngine.getEntityScreen(player);

        double um = BlobbyEngine.unitMultiplier();

        Vector2d transitionOffset = new Vector2d();
        if(currentScreen == null) {
            currentScreen = playerScreen;
        }

        entityOffset.x = currentScreen.x * 16;
        entityOffset.y = currentScreen.y * 9;

        if(playerScreen.x != currentScreen.x || playerScreen.y != currentScreen.y) {
            BlobbyEngine.transitioningScreen = true;
            screenTransition += deltaTime;
            if(screenTransition >= screenTransitionDuration) {
                screenTransition = 0;
                currentScreen = playerScreen;
                BlobbyEngine.transitioningScreen = false;
            } else {
                double screenTransitionPercentage = screenTransition / screenTransitionDuration;
                transitionOffset = new Vector2d( (playerScreen.x - currentScreen.x) * screenTransitionPercentage,
                        (playerScreen.y - currentScreen.y) * screenTransitionPercentage);
                transitionOffset.mul(16 * um, 9 * um);
            }
        }

        if(level != null && level.backgroundTexture != null) {
            render(0, 0, curWindow.getWidth(), curWindow.getHeight(), level.backgroundTexture);
        }

        Vector2d finalTransitionOffset = transitionOffset;
        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                if(screenPos.equals(playerScreen) || BlobbyEngine.transitioningScreen) {
                    screen.entities.forEach(entity -> {
                        if (entity.getTexture() != null && !entity.renderInFrontOfPlayer()) {
                            Vector2d entityPos = new Vector2d(entity.getPosition()).mul(um);
                            render((int) (entityPos.x - entityOffset.x * um - finalTransitionOffset.x),
                                    (int) (entityPos.y - entityOffset.y * um - finalTransitionOffset.y), (int) um, (int) um, entity.getTexture());
                        }
                    });
                }
            });
        }

        if(BlobbyEngine.renderPlayer) {
            render((int) (um * (player.getPosition().x - entityOffset.x) - finalTransitionOffset.x),
                    (int) (um * (player.getPosition().y - entityOffset.y) - finalTransitionOffset.y - um * 2),
                    (int) um, (int) um * 2, player.getTexture());
        }

        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                if(screenPos.equals(playerScreen) || BlobbyEngine.transitioningScreen) {
                    screen.entities.forEach(entity -> {
                        if (entity.getTexture() != null && entity.renderInFrontOfPlayer()) {
                            Vector2d entityPos = new Vector2d(entity.getPosition()).mul(um);
                            render((int) (entityPos.x - entityOffset.x * um - finalTransitionOffset.x),
                                    (int) (entityPos.y - entityOffset.y * um - finalTransitionOffset.y), (int) um, (int) um, entity.getTexture());
                        }
                    });
                }
            });
        }

        queuedTextures.forEach((pos, tex) -> render(pos.x, pos.y, pos.z, pos.w, tex));
        queuedUITextures.forEach((uvs, tex) -> renderUV(new Vector2f(uvs.x, uvs.y), new Vector2f(uvs.z, uvs.w), tex));

        if(BlobbyEngine.showMenu && BlobbyEngine.menu != null)
            renderMenu(BlobbyEngine.menu);

        glfwSwapBuffers(window.getId());
        queuedTextures.clear();
        queuedUITextures.clear();
    }

    public static void renderUV(Vector2f uvStart, Vector2f uvEnd, ITexture texture) {
        texture.bind();

        boolean flipped = texture.isFlipped();

        glBegin(GL_QUADS);
        glColor4f(texture.getColorModifiers().x, texture.getColorModifiers().y, texture.getColorModifiers().z, texture.getColorModifiers().w);
        glTexCoord2f(booleanToInt(flipped), 0);
        glVertex2f(uvStart.x, uvStart.y);
        glTexCoord2f(booleanToInt(!flipped), 0);
        glVertex2f(uvEnd.x, uvStart.y);
        glTexCoord2f(booleanToInt(!flipped), 1);
        glVertex2f(uvEnd.x, uvEnd.y);
        glTexCoord2f(booleanToInt(flipped), 1);
        glVertex2f(uvStart.x, uvEnd.y);
        glColor4f(1, 1, 1, 1);
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

        boolean flipped = texture.isFlipped();

        glBegin(GL_QUADS);
        glColor4f(texture.getColorModifiers().x, texture.getColorModifiers().y, texture.getColorModifiers().z, texture.getColorModifiers().w);
        glTexCoord2f(booleanToInt(flipped), 0);
        glVertex2f(windowXToVertexX(x), windowYToVertexY(y));
        glTexCoord2f(booleanToInt(!flipped), 0);
        glVertex2f(windowXToVertexX(x + width), windowYToVertexY(y));
        glTexCoord2f(booleanToInt(!flipped), 1);
        glVertex2f(windowXToVertexX(x + width), windowYToVertexY(y + height));
        glTexCoord2f(booleanToInt(flipped), 1);
        glVertex2f(windowXToVertexX(x), windowYToVertexY(y + height));
        glColor4f(1, 1, 1, 1);
        glEnd();
    }

    public static void setScreenTransitionDuration(double duration) {
        screenTransitionDuration = duration;
    }

    public static float windowXToVertexX(int x) {
        return (float) (2.0 * x / curWindow.getWidth() - 1);
    }

    public static float windowYToVertexY(int y) {
        return (float) (1.0 - 2.0 * y / curWindow.getHeight());
    }

}
