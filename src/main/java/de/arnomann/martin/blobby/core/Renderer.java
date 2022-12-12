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

    /** The default shader used for rendering. */
    public static final Shader defaultShader = new Shader(Shader.DEFAULT_VERTEX, Shader.DEFAULT_FRAGMENT);
    /** The default shader used for rendering UI. */
    public static final Shader uiShader = new Shader(Shader.UI_VERTEX, Shader.UI_FRAGMENT);

    /** The default camera used for rendering */
    public static final Camera defaultCamera = new Camera(-1.6f, 1.6f, -0.9f, 0.9f);
    private static final Camera uiCamera = new Camera(-1.6f, 1.6f, -0.9f, 0.9f);
    /** The active camera used for rendering. */
    public static Camera activeCamera = defaultCamera;

    private static Float[] lights = new Float[] {};

    private static final int[] QUAD_TEXTURE_COORDS = new int[] {
            0, 0, // top left
            1, 0, // top right
            1, 1, // bottom right
            0, 1  // bottom left
    };

    private static final int[] QUAD_INDICES = new int[] {
            0, 1, 2,
            2, 3, 0
    };

    private static final VertexArray VERTEX_ARRAY = new VertexArray().setTextureCoords(QUAD_TEXTURE_COORDS)
            .setIndices(QUAD_INDICES);

    private Renderer() {}

    /** ONLY FOR INTERNAL USE */
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

    /**
     * Renders a frame.
     * @param window the window to render to.
     * @param deltaTime the time between frames.
     */
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
                        playerScreen.y * defaultCamera.getHeight() * -1));
                screenTransition = 0;
                currentScreen = playerScreen;
                BlobbyEngine.transitioningScreen = false;
            } else {
                Vector2i screenDiff = new Vector2i(playerScreen).sub(currentScreen);
                defaultCamera.setPosition(cameraPositionAtScreenTransitionStart.add(new Vector2f(
                        defaultCamera.getWidth() * screenDiff.x, defaultCamera.getHeight() * screenDiff.y * -1)
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
                    if(!entity.disabled() && !(entity instanceof Block) && entity.getTexture() != null &&
                            !entity.renderInFrontOfPlayer()) {
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
                    if(!entity.disabled() && !(entity instanceof Block) && entity.getTexture() != null &&
                            entity.renderInFrontOfPlayer()) {
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

        if(BlobbyEngine.isMenuShown() && BlobbyEngine.menu != null) {
            renderMenu(BlobbyEngine.menu);
            ListenerManager.callEvent(new RenderStepDoneEvent(RenderStepDoneEvent.RenderStep.RENDER_MENU));
        }

        levelHash = BlobbyEngine.getCurrentLevel().hashCode();
        finishRendering();
    }

    /**
     * Sets the window.
     * @param window the new window.
     * */
    public static void setWindow(Window window) {
        curWindow = window;
    }

    /**
     * Finishes rendering.
     */
    public static void finishRendering() {
        glfwSwapBuffers(curWindow.getId());
        queuedTextures.clear();
        queuedUITextures.clear();

        Particle.updateParticleList();
    }

    /**
     * Renders a quad on the engine unit system.
     * @param x the x position in units.
     * @param y the y position in units.
     * @param width the width of the quad in units.
     * @param height the height of the quad in units.
     * @param texture the texture to use for the quad.
     * @param shader the shader to use for rendering the quad.
     */
    public static void renderOnUnits(float x, float y, float width, float height, ITexture texture, Shader shader) {
        x = MathUtil.scaleNumber(0, 16, activeCamera.getLeft(), activeCamera.getRight(), x);
        y = MathUtil.scaleNumber(0, 9, activeCamera.getTop(), activeCamera.getBottom(), y);
        width = MathUtil.scaleNumber(0, 8, 0, activeCamera.getRight(), width);
        height = -MathUtil.scaleNumber(0, 4.5f, 0, activeCamera.getTop(), height);

        if(!activeCamera.couldRender(x, y, x + width, y + height))
            return;

        texture.bind(0);
        shader.bind();
        shader.setUniform1i("texture", 0);

        shader.setUniformMatrix4f("viewMatrix", activeCamera.getViewMatrix());
        shader.setUniformMatrix4f("projectionMatrix", activeCamera.getProjectionMatrix());
        shader.setUniformMatrix4f("viewProjectionMatrix", activeCamera.getViewProjectionMatrix());

        for(int i = 0; i < lights.length; i += 3) {
            shader.setUniform3f("lights[" + i / 3 + "]", lights[i], lights[i + 1], lights[i + 2]);
        }

        shader.setUniform1i("lightCount", lights.length / 3);
        shader.setUniform1f("unitMultiplier", (float) BlobbyEngine.unitMultiplier());

        shader.setUniform1f("cameraWidth", activeCamera.getWidth());
        shader.setUniform1f("cameraHeight", activeCamera.getHeight());

        shader.setUniform1i("screenWidth", BlobbyEngine.getWindow().getWidth());
        shader.setUniform1i("screenHeight", BlobbyEngine.getWindow().getHeight());

        shader.setUniform1i("flipped", booleanToInt(texture.isFlipped()));

        VERTEX_ARRAY.setVertices(new float[] {
                x, y, // top left
                x + width, y,  // top right
                x + width, y + height, // bottom right
                x, y + height // bottom left
        }).setTextureCoords(QUAD_TEXTURE_COORDS).setIndices(QUAD_INDICES).render();
    }

    /**
     * Renders a quad on the UV coordinates.
     * @param uvStart the top-left UV position.
     * @param uvEnd the bottom-right UV position.
     * @param texture the texture used for the quad.
     * @param shader the shader used for rendering the quad.
     */
    public static void renderUV(Vector2f uvStart, Vector2f uvEnd, ITexture texture, Shader shader) {
        texture.bind(0);
        shader.bind();
        shader.setUniform1i("texture", 0);

        shader.setUniformMatrix4f("viewMatrix", uiCamera.getViewMatrix());
        shader.setUniformMatrix4f("projectionMatrix", uiCamera.getProjectionMatrix());
        shader.setUniformMatrix4f("viewProjectionMatrix", uiCamera.getViewProjectionMatrix());

        shader.setUniform1i("flipped", booleanToInt(texture.isFlipped()));

        Vector2f renderUVStart = new Vector2f(uvStart);
        Vector2f renderUVEnd = new Vector2f(uvEnd);
        renderUVStart.x = MathUtil.scaleNumber(-1, 1, uiCamera.getLeft(), uiCamera.getRight(), renderUVStart.x);
        renderUVStart.y = MathUtil.scaleNumber(-1, 1, uiCamera.getBottom(), uiCamera.getTop(), renderUVStart.y);
        renderUVEnd.x = MathUtil.scaleNumber(-1, 1, uiCamera.getLeft(), uiCamera.getRight(), renderUVEnd.x);
        renderUVEnd.y = MathUtil.scaleNumber(-1, 1, uiCamera.getBottom(), uiCamera.getTop(), renderUVEnd.y);

        VERTEX_ARRAY.setVertices(new float[] {
                renderUVStart.x, renderUVStart.y, // top left
                renderUVEnd.x, renderUVStart.y,  // top right
                renderUVEnd.x, renderUVEnd.y, // bottom right
                renderUVStart.x, renderUVEnd.y // bottom left
        }).setTextureCoords(QUAD_TEXTURE_COORDS).setIndices(QUAD_INDICES).render();
    }

    /**
     * Renders a menu.
     * @param menu the menu to render.
     */
    public static void renderMenu(Menu menu) {
        renderUV(new Vector2f(-1f, 1f), new Vector2f(-0.5f, -1f), menu.getBackgroundTexture(), uiShader);
        for(Button b : menu.getButtons()) {
            if(b.isVisible()) {
                renderUV(b.uvStart, b.uvEnd, b.texture, uiShader);
            }
        }
    }

    /**
     * Sets the screen transition duration.
     * @param duration the new screen transition duration.
     */
    public static void setScreenTransitionDuration(double duration) {
        screenTransitionDuration = duration;
    }

    /**
     * Returns the current screen transition duration.
     * @return the screen transition duration.
     */
    public static double getScreenTransitionDuration() {
        return screenTransitionDuration;
    }

    static class VertexArray {

        public int count;
        private int vbo;
        private int tbo;
        private int ibo;

        private static FloatBuffer VERTEX_BUFFER = BufferUtils.createFloatBuffer(8);
        private static IntBuffer TEXTURE_COORD_BUFFER = BufferUtils.createIntBuffer(8);
        private static IntBuffer INDICES_BUFFER = BufferUtils.createIntBuffer(6);

        public VertexArray() {
            vbo = glGenBuffers();
            tbo = glGenBuffers();
            ibo = glGenBuffers();
        }

        public VertexArray(float[] vertices, int[] textureCoords, int[] indices) {
            count = indices.length;

            VERTEX_BUFFER.put(vertices);
            VERTEX_BUFFER.flip();
            vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_STATIC_DRAW);
            VERTEX_BUFFER.clear();

            TEXTURE_COORD_BUFFER.put(textureCoords);
            TEXTURE_COORD_BUFFER.flip();
            tbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, tbo);
            glBufferData(GL_ARRAY_BUFFER, TEXTURE_COORD_BUFFER, GL_STATIC_DRAW);
            TEXTURE_COORD_BUFFER.clear();

            INDICES_BUFFER.put(indices);
            INDICES_BUFFER.flip();
            ibo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES_BUFFER, GL_STATIC_DRAW);
            INDICES_BUFFER.clear();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        public VertexArray setVertices(float[] vertices) {
            VERTEX_BUFFER.put(vertices);
            VERTEX_BUFFER.flip();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_STATIC_DRAW);
            VERTEX_BUFFER.clear();
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            return this;
        }

        public VertexArray setTextureCoords(int[] textureCoords) {
            TEXTURE_COORD_BUFFER.put(textureCoords);
            TEXTURE_COORD_BUFFER.flip();
            glBindBuffer(GL_ARRAY_BUFFER, tbo);
            glBufferData(GL_ARRAY_BUFFER, TEXTURE_COORD_BUFFER, GL_STATIC_DRAW);
            TEXTURE_COORD_BUFFER.clear();
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            return this;
        }

        public VertexArray setIndices(int[] indices) {
            count = indices.length;

            INDICES_BUFFER.put(indices);
            INDICES_BUFFER.flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES_BUFFER, GL_STATIC_DRAW);
            INDICES_BUFFER.clear();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            return this;
        }

        public void render() {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, tbo);
            glVertexAttribPointer(1, 2, GL_INT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);
            glDrawArrays(GL_TRIANGLES, 0, count);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

    }

}
