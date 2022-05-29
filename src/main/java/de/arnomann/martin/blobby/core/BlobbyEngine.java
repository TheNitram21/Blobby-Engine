package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.texture.AnimatedTexture;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public final class BlobbyEngine {

    public static final String TEXTURES_PATH = "textures/";
    public static final String MAPS_PATH = "maps/";
    public static final String SOUNDS_PATH = "sounds/";

    private static Window window;
    private static Map<String, ITexture> textures;
    private static Logger logger;
    private static Level currentLevel;

    public static Menu menu;
    public static boolean showMenu = false;

    private static double unitMultiplier;

    private static Player player;
    public static boolean renderPlayer = false;

    public static boolean paused = false;

    private BlobbyEngine() {}

    public static void run(RunConfigurations runConfig) {
        textures = new HashMap<>();
        logger = new Logger();

        if(runConfig.width / 16 != runConfig.height / 9) {
            throw new RuntimeException("Window size ratio not 16:9");
        }

        if(!glfwInit()) {
            ErrorManagement.showErrorMessage(logger, new IllegalStateException("GLFW couldn't be initialized!"));
            stop();
        }

        window = new Window(runConfig);
        unitMultiplier = window.getWidth() / 16;

        Input.initialize();
        Sound.initialize();

        window.start();
    }

    public static double unitMultiplier() {
        return unitMultiplier;
    }

    public static void debugMode() {
        logger.enable(Logger.LoggingType.DEBUG);
        logger.debug("Debug mode activated");
    }

    public static Vector2i getEntityScreen(Entity e) {
        Vector2i screenPos;

        Vector2d entityPos = e.getPosition();
        screenPos = new Vector2i((int) (entityPos.x / 16), (int) (entityPos.y / 9));

        return screenPos;
    }

    public static ITexture loadTexture(String filename) {
        ITexture texture;
        File file = new File(filename);

        if(file.exists()) {
            double animTimeSecs = 1;
            try(BufferedReader r = new BufferedReader(new FileReader(filename + "/animTime.txt"))) {
                animTimeSecs = Double.parseDouble(r.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }

            texture = new AnimatedTexture(animTimeSecs, filename);
        } else {
            texture = new Texture(filename);
        }

        logger.debug("filename: " + filename + " / width: " + texture.getWidth() + " / height: " + texture.getHeight());

        textures.put(filename, texture);
        return texture;
    }

    public static ITexture getTexture(String name) {
        name = TEXTURES_PATH + name;
        ITexture texture = textures.get(name);
        if(texture == null) {
            texture = loadTexture(name);
        }

        return texture;
    }

    public static void setLevel(Level level) {
        currentLevel = level;
    }

    public static Level getCurrentLevel() {
        return currentLevel;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void setPlayer(Player newPlayer) {
        player = newPlayer;
    }

    public static Window getWindow() {
        return window;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void stop() {
        if(window != null)
            glfwSetWindowShouldClose(window.getId(), true);
    }

}
