package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.texture.AnimatedTexture;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.levels.LevelLoader;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.sound.SoundPlayer;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;
import org.json.JSONObject;
import org.lwjgl.PointerBuffer;

import java.io.*;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The main class of the engine.
 */
public final class BlobbyEngine {

    /** The path to textures. */
    public static final String TEXTURES_PATH = "textures/";
    /** The path to maps. */
    public static final String MAPS_PATH = "maps/";
    /** The path to sounds. */
    public static final String SOUNDS_PATH = "sounds/";

    private static Window window;
    private static Map<String, ITexture> textures;
    private static Logger logger;
    private static Level currentLevel;

    /** The currently shown menu. */
    public static Menu menu;
    /** Whether the menu {@link BlobbyEngine#menu} should be visible or not. */
    public static boolean showMenu = false;

    private static double unitMultiplier;

    private static Player player;
    /** Whether the player should be rendered or not. */
    public static boolean renderPlayer = false;

    /** Whether the game is paused or not. Can be set from the outside. */
    public static boolean paused = false;

    static boolean transitioningScreen = false;

    private static String[] consoleArguments;

    private BlobbyEngine() {}

    /**
     * Starts the engine.
     * @param runConfig the configuration for running.
     * @param arguments the console arguments.
     */
    public static void run(RunConfigurations runConfig, String[] arguments) {
        textures = new HashMap<>();
        logger = new Logger();
        consoleArguments = arguments;

        if(runConfig.width / 16 != runConfig.height / 9) {
            throw new RuntimeException("Window size ratio not 16:9");
        }

        if(!glfwInit()) {
            ErrorManagement.showErrorMessage(logger, new IllegalStateException("GLFW couldn't be initialized!"));
            stop();
        }

        window = new Window(runConfig);
        unitMultiplier = window.getWidth() / 16d;

        Input.initialize();
        SoundPlayer.initialize();

        window.start();
    }

    static void onWindowOpen() {
        checkArguments(consoleArguments);
    }

    private static void checkArguments(String[] arguments) {
        for(String argument : arguments) {
            String[] split = argument.split(":", 1);

            String key = split[0];
            String value = split[1];

            switch(key) {
                case "map":
                    currentLevel = LevelLoader.loadLevel(value);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Creates a new entity.
     * @param classname the classname of the entity (as defined in the <i>bin/entities.json</i> file).
     * @param position the position where the entity is created.
     * @param properties the entity properties.
     * @return the created entity.
     * @throws Exception if the entity class is not found, there is no valid constructor or the constructor couldn't be called.
     */
    public static Entity instantiateEntity(String classname, Vector2d position, Map<String, Object> properties)
            throws Exception { /* We could throw:
                                    ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
                                    But that would be a bit much. */
        JSONObject entitiesJSON = loadJSON(new File("bin/entities.json"));
        String classnameWithPackage = "";

        for(Object entityObj : entitiesJSON.getJSONArray("Entities")) {
            JSONObject entityJSON = (JSONObject) entityObj;

            if(entityJSON.getString("ClassName").equals(classname)) {
                classnameWithPackage = entityJSON.getString("InternalClassName");
            }
        }

        if(classnameWithPackage.equals(""))
            throw new IllegalStateException("Couldn't find an entity with the classname " + classname + " in 'entities.json'!");

        Class<?> entityClass = Class.forName(classnameWithPackage);
        Entity entity = (Entity) entityClass.getConstructor(Vector2d.class, Map.class).newInstance(position, properties);
        ListenerManager.registerEventListener(entity);
        return entity;
    }

    /**
     * Loads a JSON from a file.
     * @param path the path to the JSON file.
     * @return the loaded JSON.
     */
    public static JSONObject loadJSON(File path) {
        JSONObject json;

        if(!path.exists()) {
            ErrorManagement.showErrorMessage(logger, new FileNotFoundException("File " + path + " doesn't exist!"));
        }

        StringBuilder jsonTextBuilder = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonTextBuilder.append(line).append("\n");
            }
        } catch(IOException e) {
            ErrorManagement.showErrorMessage(logger, e);
        }

        json = new JSONObject(jsonTextBuilder.toString());
        return json;
    }

    /**
     * Returns the amount of monitors connected to the computer.
     * @return the monitor count.
     */
    public static int getMonitorCount() {
        PointerBuffer monitors = glfwGetMonitors();
        return monitors.position() + monitors.remaining();
    }

    /**
     * Returns whether we are transitioning between to screens or not.
     * @return {@code true} if the game is transitioning between two screens, {@code false} otherwise.
     */
    public static boolean isTransitioningBetweenScreens() {
        return transitioningScreen;
    }

    /**
     * Returns the unit multiplier. Every world position is multiplied by this to get the screen position.
     * @return the unit multiplier.
     */
    public static double unitMultiplier() {
        return unitMultiplier;
    }

    /**
     * Enables the debug mode.
     */
    public static void debugMode() {
        logger.enable(Logger.LoggingType.DEBUG);
        logger.debug("Debug mode activated");
    }

    /**
     * Returns the screen of a specific entity.
     * @param e the entity.
     * @return the screen.
     */
    public static Vector2i getEntityScreen(Entity e) {
        return new Vector2i((int) Math.floor((e.getPosition().x + e.getWidth() / 2d) / 16), (int) Math.floor((e.getPosition().y + e.getHeight() / 2d) / 9));
    }

    /**
     * Loads a texture from a path.
     * @param filename the texture path.
     * @return the texture.
     */
    public static ITexture loadTexture(String filename) {
        ITexture texture = getTextureWithoutCaching(filename);
        logger.debug("Loaded texture " + filename + " with size " + texture.getWidth() + ", " + texture.getHeight() + ".");
        textures.put(filename, texture);
        return texture;
    }

    /**
     * Gets a texture from a path. Loads the texture only if it hasn't been loaded yet.
     * @param name the path to the texture.
     * @return the texture.
     */
    public static ITexture getTexture(String name) {
        ITexture texture = textures.get(name);
        if(texture == null) {
            texture = loadTexture(name);
        }

        return texture;
    }

    /**
     * Loads a texture from a path. DOES NOT cache it.
     * @param filename the path of the texture.
     * @return the texture.
     */
    public static ITexture getTextureWithoutCaching(String filename) {
        ITexture texture;
        filename = TEXTURES_PATH + filename;
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

        return texture;
    }

    /**
     * Sets the current level.
     * @param level the new current level.
     * @see BlobbyEngine#getCurrentLevel()
     */
    public static void setLevel(Level level) {
        currentLevel = level;
    }

    /**
     * Returns the current level.
     * @return the current level.
     * @see BlobbyEngine#setLevel(Level)
     */
    public static Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Returns the player.
     * @return the player.
     */
    public static Player getPlayer() {
        return player;
    }

    /**
     * Sets the player.
     * @param newPlayer the new player.
     */
    public static void setPlayer(Player newPlayer) {
        player = newPlayer;
    }

    /**
     * Returns the window so that you can perform operations like resizing.
     * @return the window.
     * @see Window
     */
    public static Window getWindow() {
        return window;
    }

    /**
     * Returns the logger. You should create your own logger though.
     * @return the logger.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Shuts down the engine.
     */
    public static void stop() {
        if(window != null)
            glfwSetWindowShouldClose(window.getId(), true);
    }

}
