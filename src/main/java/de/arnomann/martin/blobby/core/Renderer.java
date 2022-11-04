package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Particle;
import de.arnomann.martin.blobby.entity.Block;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.entity.Light;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.RenderStepDoneEvent;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static de.arnomann.martin.blobby.MathUtil.booleanToInt;

/**
 * An <b>INTERNAL</b> class for rendering.
 */
public final class Renderer {

    private static Map<Vector4i, ITexture> queuedTextures = new HashMap<>();
    private static Map<Vector4f, ITexture> queuedUITextures = new HashMap<>();

    private static Window curWindow;

    private static Vector2i currentScreen;
    private static Vector2f cameraPositionAtScreenTransitionStart;
    private static double screenTransition = 0d;
    private static int levelHash = 0;

    private static double screenTransitionDuration = 1d; // seconds

    public static final Shader defaultShader = new Shader(Shader.DEFAULT_VERTEX, Shader.DEFAULT_FRAGMENT);
    public static final Shader uiShader = new Shader(Shader.UI_VERTEX, Shader.UI_FRAGMENT);

    public static final Camera defaultCamera = new Camera(-1.6f, 1.6f, -0.9f, 0.9f);
    private static final Camera uiCamera = new Camera(-1.6f, 1.6f, -0.9f, 0.9f);
    public static Camera activeCamera = defaultCamera;

    private static Float[] lights = new Float[] {};

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

        List<Float> lights = new ArrayList<>();
        if(level != null) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Light))
                    continue;

                lights.add((float) (entity.getPosition().x + 0.5f));
                lights.add((float) (entity.getPosition().y + 0.5f));
                lights.add(Float.valueOf(entity.getParameters().get("Radius")));
            }

            Renderer.lights = new Float[lights.size()];
            for(int i = 0; i < lights.size(); i++)
                Renderer.lights[i] = lights.get(i);
        } else {
            Renderer.lights = new Float[] {};
        }

        if(currentScreen == null) {
            currentScreen = playerScreen;
        }

        if(playerScreen.x != currentScreen.x || playerScreen.y != currentScreen.y) {
            if(screenTransition == 0)
                cameraPositionAtScreenTransitionStart = defaultCamera.getPosition();

            BlobbyEngine.transitioningScreen = true;
            screenTransition += deltaTime;

            if(BlobbyEngine.getCurrentLevel().hashCode() != levelHash)
                screenTransition = screenTransitionDuration;

            if(screenTransition >= screenTransitionDuration) {
                defaultCamera.setPosition(new Vector2f(playerScreen.x * defaultCamera.getWidth(),
                        playerScreen.y * defaultCamera.getHeight()));
                screenTransition = 0;
                currentScreen = playerScreen;
                BlobbyEngine.transitioningScreen = false;
            } else {
                Vector2i screenDiff = new Vector2i(playerScreen).sub(currentScreen);
                defaultCamera.setPosition(cameraPositionAtScreenTransitionStart.add(new Vector2f(
                        defaultCamera.getWidth() * screenDiff.x, defaultCamera.getHeight() * screenDiff.y)
                        .mul((float) (deltaTime / screenTransitionDuration))));
            }
        }

        if(level != null && level.backgroundTexture != null) {
            Vector2i backgroundSize = new Vector2i(level.getWidthInScreens() * 16, level.getHeightInScreens() * 9);

            renderOnUnits(level.getFirstScreenX() * 16, level.getFirstScreenY() * 9, backgroundSize.x, backgroundSize.y,
                    level.backgroundTexture, defaultShader);
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_BACKGROUND));

        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                screen.entities.forEach(entity -> {
                    if(!(entity instanceof Block) && entity.getTexture() != null && !entity.renderInFrontOfPlayer()) {
                        Vector2d entityPos = new Vector2d(entity.getPosition()).add(entity.getRenderingOffset());
                        renderOnUnits((float) entityPos.x, (float) entityPos.y, entity.getWidth(), entity.getHeight(),
                                entity.getTexture(), (entity.getShader() != null ? entity.getShader() : defaultShader));
                    }
                });
            });
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_ENTITIES_BEHIND_PLAYER));

        if(BlobbyEngine.renderPlayer) {
            renderOnUnits((float) player.getPosition().x, (float) player.getPosition().y - player.getHeight(),
                    player.getWidth(), player.getHeight(), player.getTexture(),
                    (player.getShader() != null ? player.getShader() : defaultShader));
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_PLAYER));

        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                screen.entities.forEach(entity -> {
                    if(entity instanceof Block && entity.getTexture() != null) {
                        Vector2d entityPos = new Vector2d(entity.getPosition()).add(entity.getRenderingOffset());
                        renderOnUnits((float) entityPos.x, (float) entityPos.y, entity.getWidth(), entity.getHeight(),
                                entity.getTexture(), (entity.getShader() != null ? entity.getShader() : defaultShader));
                    }
                });
            });
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_BLOCKS));

        if(level != null) {
            level.screens.forEach((screenPos, screen) -> {
                screen.entities.forEach(entity -> {
                    if(!(entity instanceof Block) && entity.getTexture() != null && entity.renderInFrontOfPlayer()) {
                        Vector2d entityPos = new Vector2d(entity.getPosition()).add(entity.getRenderingOffset());
                        renderOnUnits((float) entityPos.x, (float) entityPos.y, entity.getWidth(), entity.getHeight(),
                                entity.getTexture(), (entity.getShader() != null ? entity.getShader() : defaultShader));
                    }
                });
            });
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_ENTITIES_IN_FRONT_OF_PLAYER));

        for(Particle particle : Particle.getParticles()) {
            Vector2d particlePos = new Vector2d(particle.getPosition());
            renderOnUnits((float) particlePos.x, (float) particlePos.y, 1, 1, particle, defaultShader);
        }
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_PARTICLES));

        queuedTextures.forEach((pos, tex) -> renderOnUnits(pos.x, pos.y, pos.z, pos.w, tex, defaultShader));
        queuedUITextures.forEach((uvs, tex) -> renderUV(new Vector2f(uvs.x, uvs.y), new Vector2f(uvs.z, uvs.w), tex, uiShader));
        ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_QUEUED_TEXTURES));

        if(BlobbyEngine.showMenu && BlobbyEngine.menu != null) {
            renderMenu(BlobbyEngine.menu);
            ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_MENU));
        }

        levelHash = BlobbyEngine.getCurrentLevel().hashCode();
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

    public static void renderOnUnits(float x, float y, float width, float height, ITexture texture, Shader shader) {
        texture.bind(0);
        shader.bind();
        glUniform1i(glGetUniformLocation(shader.id, "texture"), 0);

        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        activeCamera.getViewMatrix().get(matrixBuffer);
        glUniformMatrix4fv(glGetUniformLocation(shader.id, "viewMatrix"), false, matrixBuffer);
        activeCamera.getProjectionMatrix().get(matrixBuffer);
        glUniformMatrix4fv(glGetUniformLocation(shader.id, "projectionMatrix"), false, matrixBuffer);
        activeCamera.getViewProjectionMatrix().get(matrixBuffer);
        glUniformMatrix4fv(glGetUniformLocation(shader.id, "viewProjectionMatrix"), false, matrixBuffer);

        for(int i = 0; i < lights.length; i += 3) {
            glUniform3f(glGetUniformLocation(shader.id, "lights[" + i + "]"), lights[i],
                    lights[i + 1], lights[i + 2]);
        }

        glUniform1i(glGetUniformLocation(shader.id, "lightCount"), lights.length / 3);
        glUniform1f(glGetUniformLocation(shader.id, "unitMultiplier"), (float) BlobbyEngine.unitMultiplier());

        glUniform1f(glGetUniformLocation(shader.id, "cameraWidth"), activeCamera.getWidth());
        glUniform1f(glGetUniformLocation(shader.id, "cameraHeight"), activeCamera.getHeight());

        glUniform1i(glGetUniformLocation(shader.id, "screenWidth"), BlobbyEngine.getWindow().getWidth());
        glUniform1i(glGetUniformLocation(shader.id, "screenHeight"), BlobbyEngine.getWindow().getHeight());

        boolean flipped = texture.isFlipped();

        x = MathUtil.scaleNumber(0, 16, activeCamera.getLeft(), activeCamera.getRight(), x);
        y = MathUtil.scaleNumber(0, 9, activeCamera.getTop(), activeCamera.getBottom(), y);
        width = MathUtil.scaleNumber(0, 8, 0, activeCamera.getRight(), width);
        height = -MathUtil.scaleNumber(0, 4.5f, 0, activeCamera.getTop(), height);

        new VertexArray(new float[] {
                x, y, // top left
                x + width, y,  // top right
                x + width, y + height, // bottom right
                x, y + height // bottom left
        }, new int[] {
                booleanToInt(flipped), 0,
                booleanToInt(!flipped), 0,
                booleanToInt(!flipped), 1,
                booleanToInt(flipped), 1
        }, new int[] {
                0, 1, 2,
                2, 3, 0
        }).render();
    }

    public static void renderUV(Vector2f uvStart, Vector2f uvEnd, ITexture texture, Shader shader) {
        texture.bind(0);
        shader.bind();
        glUniform1i(glGetUniformLocation(shader.id, "texture"), 0);

        FloatBuffer viewProjectionBuffer = BufferUtils.createFloatBuffer(16);
        uiCamera.getViewProjectionMatrix().get(viewProjectionBuffer);
        glUniformMatrix4fv(glGetUniformLocation(shader.id, "viewProjectionMatrix"), false, viewProjectionBuffer);

        boolean flipped = texture.isFlipped();

        uvStart.x = MathUtil.scaleNumber(-1, 1, uiCamera.getLeft(), uiCamera.getRight(), uvStart.x);
        uvStart.y = MathUtil.scaleNumber(-1, 1, uiCamera.getBottom(), uiCamera.getTop(), uvStart.y);
        uvEnd.x = MathUtil.scaleNumber(-1, 1, uiCamera.getLeft(), uiCamera.getRight(), uvEnd.x);
        uvEnd.y = MathUtil.scaleNumber(-1, 1, uiCamera.getBottom(), uiCamera.getTop(), uvEnd.y);

        new VertexArray(new float[] {
                uvStart.x, uvStart.y, // top left
                uvEnd.x, uvStart.y,  // top right
                uvEnd.x, uvEnd.y, // bottom right
                uvStart.x, uvEnd.y // bottom left
        }, new int[] {
                booleanToInt(flipped), 0,
                booleanToInt(!flipped), 0,
                booleanToInt(!flipped), 1,
                booleanToInt(flipped), 1
        }, new int[] {
                0, 1, 2,
                2, 3, 0
        }).render();
    }

    public static void renderMenu(Menu menu) {
        renderUV(new Vector2f(-1f, 1f), new Vector2f(-0.5f, -1f), menu.getBackgroundTexture(), uiShader);
        for(Button b : menu.getButtons()) {
            renderUV(b.uvStart, b.uvEnd, b.texture, uiShader);
        }
    }

    public static void setScreenTransitionDuration(double duration) {
        screenTransitionDuration = duration;
    }

    public static double getScreenTransitionDuration() {
        return screenTransitionDuration;
    }

    public static float windowXToVertexX(int x) {
        return (float) (2.0 * x / curWindow.getWidth() - 1);
    }

    public static float windowYToVertexY(int y) {
        return (float) (1.0 - 2.0 * y / curWindow.getHeight());
    }

    static class VertexArray {

        public final int count;
        private final int vbo;
        private final int tbo;
        private final int ibo;

        public VertexArray(float[] vertices, int[] textureCoords, int[] indices) {
            count = indices.length;

            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
            vertexBuffer.put(vertices);
            vertexBuffer.flip();
            vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

            IntBuffer textureCoordBuffer = BufferUtils.createIntBuffer(textureCoords.length);
            textureCoordBuffer.put(textureCoords);
            textureCoordBuffer.flip();
            tbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, tbo);
            glBufferData(GL_ARRAY_BUFFER, textureCoordBuffer, GL_STATIC_DRAW);

            IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices);
            indicesBuffer.flip();
            ibo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        public void render() {
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, tbo);
            glVertexAttribPointer(1, 2, GL_INT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);
            glDrawArrays(GL_TRIANGLES, 0, count);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
        }

    }

}
