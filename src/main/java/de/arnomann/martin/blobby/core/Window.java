package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.StartEvent;
import de.arnomann.martin.blobby.event.UpdateEvent;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public final class Window {

    private final long windowId;

    private String title;
    private final int width;
    private final int height;

    public Window(RunConfigurations runConfig) {
        this.title = runConfig.title;
        this.width = runConfig.width;
        this.height = runConfig.height;

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        windowId = glfwCreateWindow(width, height, title, 0, 0);
        if(windowId == 0) {
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(), new RuntimeException("An unexpected error occurred whilst trying create the window."));
            BlobbyEngine.stop();
        }

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowId, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(windowId,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(windowId);
        glfwSwapInterval(1);

        show();
    }

    void start() {
        GL.createCapabilities();

        glClearColor(0.2f, 0.3f, 1f, 0f);

        glEnable(GL_TEXTURE_2D);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        ListenerManager.callEvent(new StartEvent());
        BlobbyEngine.onWindowOpen();

        double lastFrameTime = glfwGetTime();
        while(!glfwWindowShouldClose(windowId)) {
            double curTime = glfwGetTime();
            float delta = (float) (curTime - lastFrameTime);
            ListenerManager.callEvent(new UpdateEvent(delta, curTime));

            Renderer.render(this, delta);

            lastFrameTime = curTime;

            glfwPollEvents();
        }

        if(Sound.isInitialized())
            Sound.destroy();

        close();

        glfwTerminate();
    }

    void close() {
        hide();
        glfwDestroyWindow(windowId);
    }

    private void show() {
        glfwShowWindow(windowId);
    }

    private void hide() {
        glfwHideWindow(windowId);
    }

    public long getId() {
        return windowId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setTitle(String title) {
        this.title = title;
        glfwSetWindowTitle(windowId, title);
    }

}
