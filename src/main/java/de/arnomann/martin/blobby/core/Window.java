package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.event.LateUpdateEvent;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.StartEvent;
import de.arnomann.martin.blobby.event.UpdateEvent;
import de.arnomann.martin.blobby.sound.SoundPlayer;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Represents a basic window.
 */
public final class Window {

    private final long windowId;

    private String title;
    private int windowX;
    private int windowY;
    private int width;
    private int height;
    private final String iconPath;
    private boolean fullscreen;

    private boolean started = false;

    private Framebuffer framebuffer;

    /**
     * The maximum amount of frames rendered each second. Negative values will make the framerate infinite.
     */
    public int maxFramerate = 60;
    private boolean vSync = false;

    /**
     * Creates a new window. SHOULD ONLY BE CALLED FROM THE BLOBBY ENGINE CLASS.
     * @param runConfig the run configuration.
     */
    public Window(RunConfigurations runConfig) {
        this.title = runConfig.title;
        this.width = runConfig.width;
        this.height = runConfig.height;
        this.iconPath = runConfig.iconPath;
        this.fullscreen = runConfig.fullscreen;

        if(runConfig.fullscreen) {
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            this.width = vidMode.width();
            this.height = vidMode.height();
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        windowId = glfwCreateWindow(width, height, title, runConfig.fullscreen ? glfwGetPrimaryMonitor() : 0, 0);
        if(windowId == 0) {
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(),
                    new RuntimeException("An unexpected error occurred whilst trying create the window."));
            BlobbyEngine.stop();
        }

        if(!runConfig.fullscreen) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);

                glfwGetWindowSize(windowId, pWidth, pHeight);

                GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

                glfwSetWindowPos(windowId,
                        (vidMode.width() - pWidth.get(0)) / 2,
                        (vidMode.height() - pHeight.get(0)) / 2);
            }
        }

        glfwMakeContextCurrent(windowId);
        glfwSwapInterval(1);

        glfwSetWindowPosCallback(windowId, (id, x, y) -> {
            if(fullscreen)
                return;

            windowX = x;
            windowY = y;
        });
        glfwSetWindowSizeCallback(windowId, (id, width, height) -> glViewport(0, 0, width, height));

        show();
    }

    /**
     * Starts the window.
     */
    void start() {
        if(!started) {
            started = true;

            GL.createCapabilities();

            glClearColor(0.2f, 0.3f, 1f, 0f);

            glEnable(GL_TEXTURE_2D);
            glEnable(GL_MULTISAMPLE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDepthFunc(GL_LEQUAL);

            framebuffer = new Framebuffer(width, height);
            framebuffer.unbind();

            Texture iconTexture;

            if(iconPath != null && !iconPath.isBlank())
                iconTexture = (Texture) BlobbyEngine.getTexture(iconPath);
            else
                iconTexture = BlobbyEngine.getDefaultWindowIcon();

            GLFWImage.Buffer iconBuffer = GLFWImage.create(1);
            GLFWImage iconImage = GLFWImage.create().set(iconTexture.getWidth(), iconTexture.getHeight(), iconTexture.getPixels());
            iconBuffer.put(0, iconImage);
            glfwSetWindowIcon(windowId, iconBuffer);

            ListenerManager.callEvent(new StartEvent());
            BlobbyEngine.onWindowOpen();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            double lastFrameTime = glfwGetTime();
            while(!glfwWindowShouldClose(windowId)) {
                if(maxFramerate <= 0)
                    maxFramerate = -1;

                boolean debugMode = BlobbyEngine.isDebugMode();

                double curTime = glfwGetTime();
                float delta = (float) (curTime - lastFrameTime);

                if(maxFramerate < 0 || delta >= (1f / maxFramerate)) {
                    double updateStartTime = 0;

                    if(debugMode)
                        updateStartTime = glfwGetTime();
                    ListenerManager.callEvent(new UpdateEvent(delta, curTime));
                    ListenerManager.callEvent(new LateUpdateEvent(delta, curTime));
                    if(debugMode)
                        BlobbyEngine.getProfiler().updateUpdateTime((float) (glfwGetTime() - updateStartTime));

                    double renderStartTime = 0;

                    if(debugMode)
                        renderStartTime = glfwGetTime();
                    Renderer.render(this, delta);
                    if(debugMode)
                        BlobbyEngine.getProfiler().updateRenderTime((float) (glfwGetTime() - renderStartTime));

                    lastFrameTime = curTime;
                    if(debugMode)
                        BlobbyEngine.getProfiler().updateFrameTime(delta);

                    glfwPollEvents();
                }
            }

            if(SoundPlayer.isInitialized())
                SoundPlayer.destroy();

            close();
            if(BlobbyEngine.isDebugMode())
                BlobbyEngine.getProfiler().destroy();

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            glfwTerminate();
        }
    }

    void close() {
        BlobbyEngine.getLogger().info("Shutting down engine...");
        hide();
        glfwDestroyWindow(windowId);
    }

    private void show() {
        glfwShowWindow(windowId);
    }

    private void hide() {
        glfwHideWindow(windowId);
    }

    /**
     * Returns the id of the window
     * @return the id.
     */
    public long getId() {
        return windowId;
    }

    /**
     * Returns the width of the window in pixels.
     * @return the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the window in pixels.
     * @return the height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the title of the window.
     * @param title the new window.
     */
    public void setTitle(String title) {
        this.title = title;
        glfwSetWindowTitle(windowId, title);
    }

    /**
     * Sets the size of the window. Has to be an aspect ratio of 16:9.
     * @param width the new width.
     * @param height the new height.
     * @return {@code true} if the aspect ratio is 16:9 and the window size was changed, {@code false} otherwise.
     */
    public boolean setWindowSize(int width, int height) {
        if(width / 16 != height / 9)
            return false;

        this.width = width;
        this.height = height;

        glfwSetWindowSize(windowId, this.width, this.height);
        BlobbyEngine.recalculateUnitMultiplier();

        return true;
    }

    /**
     * Sets the window to fullscreen or windowed mode.
     * @param fullscreen {@code true} if the window should fill the entire screen, {@code false} otherwise.
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;

        if(fullscreen) {
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowMonitor(windowId, glfwGetPrimaryMonitor(), 0, 0, videoMode.width(), videoMode.height(),
                    GLFW_DONT_CARE);
        } else {
            glfwSetWindowMonitor(windowId, 0, windowX, windowY, width, height, 0);
        }

    }

    /**
     * Returns whether the window is in fullscreen mode.
     * @return {@code true} if the window fills the entire screen, {@code false} otherwise.
     */
    public boolean getFullscreen() {
        return fullscreen;
    }

    /**
     * Returns an alternate framebuffer.
     * @return the framebuffer.
     */
    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

    /**
     * Sets whether VSync should be enabled or not. VSync means that OpenGL will only render as many frames as the
     * screen can show.
     * @param vSync whether VSync is enabled or not.
     * @see Window#isVSyncEnabled()
     */
    public void setVSyncEnabled(boolean vSync) {
        glfwSwapInterval(MathUtil.booleanToInt(vSync));
        this.vSync = vSync;
    }

    /**
     * Returns whether VSync is currently enabled or not.
     * @return {@code true} if VSync is enabled, {@code false} otherwise.
     * @see Window#setVSyncEnabled(boolean)
     */
    public boolean isVSyncEnabled() {
        return vSync;
    }

    /**
     * Returns the size of the window.
     * @return the window size.
     */
    public Vector2i getWindowSize() {
        return new Vector2i(width, height);
    }

}
