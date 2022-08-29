package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Particle;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.RenderStepDoneEvent;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static de.arnomann.martin.blobby.MathUtil.booleanToInt;

/**
 * An <b>INTERNAL</b> class for rendering.
 */
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

    /**
     * Queues a texture for rendering.
     * @param x the x position in pixels from the left of the window.
     * @param y the y position in pixels from the top of the window.
     * @param width the width in pixels.
     * @param height the height in pixels.
     * @param texture the texture to queue for rendering.
     */
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

        entityOffset.x = currentScreen.x * 16;
        entityOffset.y = currentScreen.y * 9;

        Vector2d finalTransitionOffset = transitionOffset;
        if(level != null && level.backgroundTexture != null) {
            Vector2i backgroundSize = new Vector2i((int) (level.getWidthInScreens() * 16 * um), (int) (level.getHeightInScreens() * 9 * um));

            render((int) ((level.getFirstScreenX() - currentScreen.x) * 16 * um - finalTransitionOffset.x),
                    (int) ((level.getFirstScreenY() - currentScreen.y) * 9 * um - finalTransitionOffset.y), backgroundSize.x, backgroundSize.y,
                    level.backgroundTexture);
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_BACKGROUND));

        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                screen.entities.forEach(entity -> {
                    if(entity.getTexture() != null && !entity.renderInFrontOfPlayer()) {
                        Vector2d entityPos = new Vector2d(entity.getPosition()).add(entity.getRenderingOffset()).mul(um);
                        render((int) (entityPos.x - entityOffset.x * um - finalTransitionOffset.x),
                                (int) (entityPos.y - entityOffset.y * um - finalTransitionOffset.y), (int) um, (int) um, entity.getTexture());
                    }
                });
            });
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_ENTITIES_BEHIND_PLAYER));

        if(BlobbyEngine.renderPlayer) {
            render((int) (um * (player.getPosition().x - entityOffset.x) - finalTransitionOffset.x),
                    (int) (um * (player.getPosition().y - entityOffset.y) - finalTransitionOffset.y - um * 2),
                    (int) um, (int) um * 2, player.getTexture());
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_PLAYER));

        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                screen.entities.forEach(entity -> {
                    if(entity.getTexture() != null && entity.renderInFrontOfPlayer()) {
                        Vector2d entityPos = new Vector2d(entity.getPosition()).add(entity.getRenderingOffset()).mul(um);
                        render((int) (entityPos.x - entityOffset.x * um - finalTransitionOffset.x),
                                (int) (entityPos.y - entityOffset.y * um - finalTransitionOffset.y), (int) um, (int) um, entity.getTexture());
                    }
                });
            });
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_ENTITIES_IN_FRONT_OF_PLAYER));

        if(level != null && level.lightMapTexture != null) {
            Vector2i backgroundSize = new Vector2i((int) (level.getWidthInScreens() * 16 * um), (int) (level.getHeightInScreens() * 9 * um));

            render((int) ((level.getFirstScreenX() - currentScreen.x) * 16 * um - finalTransitionOffset.x),
                    (int) ((level.getFirstScreenY() - currentScreen.y) * 9 * um - finalTransitionOffset.y), backgroundSize.x, backgroundSize.y,
                    level.lightMapTexture);
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_LIGHT_MAP));

        for(Particle particle : Particle.getParticles()) {
            Vector2d particlePos = new Vector2d(particle.getPosition()).mul(um);
            render((int) (particlePos.x - entityOffset.x * um - finalTransitionOffset.x),
                    (int) (particlePos.y - entityOffset.y * um - finalTransitionOffset.y), (int) um, (int) um, particle);
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_PARTICLES));

        queuedTextures.forEach((pos, tex) -> render(pos.x, pos.y, pos.z, pos.w, tex));
        queuedUITextures.forEach((uvs, tex) -> renderUV(new Vector2f(uvs.x, uvs.y), new Vector2f(uvs.z, uvs.w), tex));
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_QUEUED_TEXTURES));

        if(BlobbyEngine.showMenu && BlobbyEngine.menu != null) {
            renderMenu(BlobbyEngine.menu);
            ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_MENU));
        }

        finishRendering();
    }

    public static void setWindow(Window window) {
        curWindow = window;
    }

    public static void finishRendering() {
        glfwSwapBuffers(curWindow.getId());
        queuedTextures.clear();
        queuedUITextures.clear();

        Particle.updateParticleList();
    }

    public static void renderOnUnits(float x, float y, float width, float height, ITexture texture) {
        texture.bind();

        boolean flipped = texture.isFlipped();

        x = MathUtil.scaleNumber(0, 16, -1, 1, x);
        y = MathUtil.scaleNumber(0, 9, 1, -1, y);
        width = MathUtil.scaleNumber(0, 8, 0, 1, width);
        height = MathUtil.scaleNumber(0, 9, -1, 1, height);

        glBegin(GL_QUADS);
        glColor4f(texture.getColorModifiers().x, texture.getColorModifiers().y, texture.getColorModifiers().z, texture.getColorModifiers().w);
        glTexCoord2f(booleanToInt(flipped), 0);
        glVertex2f(x, y);
        glTexCoord2f(booleanToInt(!flipped), 0);
        glVertex2f(x + width, y);
        glTexCoord2f(booleanToInt(!flipped), 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(booleanToInt(flipped), 1);
        glVertex2f(x, y + height);
        glColor4f(1, 1, 1, 1);
        glEnd();
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
        renderUV(new Vector2f(-1f, 1f), new Vector2f(-0.5f, -1f), menu.getBackgroundTexture());
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
